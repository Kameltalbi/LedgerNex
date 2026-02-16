package com.ledgernex.app.ui.util

import android.content.Context
import android.net.Uri
import com.ledgernex.app.data.entity.Transaction
import com.ledgernex.app.data.entity.TransactionType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Utility class for importing transactions from CSV files.
 * Expected CSV format:
 * date, type (RECETTE/DEPENSE), libelle, objet, montant, categorie, accountId
 * Example:
 * 15/02/2024, DEPENSE, Achat fournitures, Papier et stylo, 45.50, Fournitures, 1
 */
class CsvTransactionImporter {

    data class ImportResult(
        val successCount: Int,
        val errorCount: Int,
        val errors: List<String>
    )

    companion object {
        // Supported date formats
        private val DATE_FORMATTERS = listOf(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        )

        fun importFromUri(context: Context, uri: Uri): Pair<List<Transaction>, List<String>> {
            val transactions = mutableListOf<Transaction>()
            val errors = mutableListOf<String>()

            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                        var lineNumber = 0
                        lines.forEach { line ->
                            lineNumber++
                            // Skip header line
                            if (lineNumber == 1 && isHeaderLine(line)) {
                                return@forEach
                            }

                            try {
                                parseTransaction(line, lineNumber)?.let { transaction ->
                                    transactions.add(transaction)
                                }
                            } catch (e: Exception) {
                                errors.add("Ligne $lineNumber: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                errors.add("Erreur lors de la lecture du fichier: ${e.message}")
            }

            return Pair(transactions, errors)
        }

        private fun isHeaderLine(line: String): Boolean {
            val lower = line.lowercase()
            return lower.contains("date") || 
                   lower.contains("type") || 
                   lower.contains("libelle") || 
                   lower.contains("montant")
        }

        private fun parseTransaction(line: String, lineNumber: Int): Transaction? {
            // Handle quoted values and split by comma
            val values = parseCsvLine(line)
            
            if (values.size < 6) {
                throw Exception("Format invalide: ${values.size} colonnes trouvées, minimum 6 requises (date, type, libelle, objet, montant, categorie)")
            }

            val dateStr = values[0].trim()
            val typeStr = values[1].trim()
            val libelle = values[2].trim()
            val objet = values[3].trim()
            val montantStr = values[4].trim().replace(",", ".") // Handle French decimal
            val categorie = values[5].trim()
            val accountId = if (values.size > 6) values[6].trim().toLongOrNull() ?: 1L else 1L

            // Parse date
            val date = parseDate(dateStr) 
                ?: throw Exception("Format de date invalide: $dateStr (formats supportés: dd/MM/yyyy, yyyy-MM-dd, MM/dd/yyyy)")

            // Parse type
            val type = when (typeStr.uppercase()) {
                "RECETTE", "REVENU", "INCOME", "R" -> TransactionType.RECETTE
                "DEPENSE", "EXPENSE", "D", "CHARGE" -> TransactionType.DEPENSE
                else -> throw Exception("Type invalide: $typeStr (utilisez RECETTE ou DEPENSE)")
            }

            // Parse amount
            val montant = montantStr.toDoubleOrNull() 
                ?: throw Exception("Montant invalide: $montantStr")

            if (montant <= 0) {
                throw Exception("Le montant doit être positif")
            }

            if (libelle.isBlank()) {
                throw Exception("Le libellé ne peut pas être vide")
            }

            return Transaction(
                id = 0,
                type = type,
                dateEpoch = date.toEpochDay(),
                libelle = libelle,
                objet = objet.ifBlank { "" },
                montantTTC = montant,
                categorie = categorie.ifBlank { "Divers" },
                accountId = accountId
            )
        }

        private fun parseDate(dateStr: String): LocalDate? {
            for (formatter in DATE_FORMATTERS) {
                try {
                    return LocalDate.parse(dateStr, formatter)
                } catch (_: DateTimeParseException) {
                    // Try next format
                }
            }
            return null
        }

        private fun parseCsvLine(line: String): List<String> {
            val result = mutableListOf<String>()
            val sb = StringBuilder()
            var inQuotes = false

            for (char in line) {
                when {
                    char == '"' -> {
                        inQuotes = !inQuotes
                    }
                    char == ',' && !inQuotes -> {
                        result.add(sb.toString().trim())
                        sb.clear()
                    }
                    else -> sb.append(char)
                }
            }
            result.add(sb.toString().trim())
            return result
        }

        /**
         * Generates a sample CSV content for users to understand the format
         */
        fun generateSampleCsv(): String {
            return """date,type,libelle,objet,montant,categorie,accountId
15/02/2024,DEPENSE,Achat fournitures,Papier et stylos,45.50,Fournitures,1
10/02/2024,RECETTE,Vente client,Produit A,1250.00,Ventes,1
05/02/2024,DEPENSE,Frais transport,Déplacement client,35.20,Transport,1
01/02/2024,RECETTE,Prestation service,Consulting,500.00,Prestations,2""".trimIndent()
        }
    }
}
