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
        // Supported date formats (d = 1 or 2 digits for day/month)
        private val DATE_FORMATTERS = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        )

        fun importFromUri(context: Context, uri: Uri): Pair<List<Transaction>, List<String>> {
            val transactions = mutableListOf<Transaction>()
            val errors = mutableListOf<String>()

            try {
                // Take persistable permission for the URI
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri, 
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Permission might already be granted, continue
                }
                
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val allLines = BufferedReader(InputStreamReader(inputStream)).readLines()
                    if (allLines.isEmpty()) {
                        errors.add("Fichier vide")
                        return Pair(transactions, errors)
                    }
                    val firstLine = allLines[0]
                    // Détecter le séparateur (virgule ou point-virgule, fréquent dans les exports Excel EU/banque)
                    val delimiter = detectDelimiter(firstLine)
                    val headerMap = parseBankHeader(firstLine, delimiter)
                    if (headerMap != null) {
                        // Format export banque (colonnes nommées)
                        for (i in 1 until allLines.size) {
                            val line = allLines[i]
                            if (line.isBlank()) continue
                            val values = parseCsvLine(line, delimiter)
                            try {
                                val tx = parseBankRow(values, headerMap, i + 1)
                                if (tx != null) transactions.add(tx)
                                else errors.add("Ligne ${i + 1}: ignorée (date, montant ou libellé manquant/invalide)")
                            } catch (e: Exception) {
                                errors.add("Ligne ${i + 1}: ${e.message}")
                            }
                        }
                    } else {
                        // Format LedgerNex classique (ordre fixe)
                        for (i in allLines.indices) {
                            val lineNumber = i + 1
                            if (lineNumber == 1 && isHeaderLine(allLines[i])) continue
                            try {
                                parseTransaction(allLines[i], lineNumber, delimiter)?.let {
                                    transactions.add(it)
                                }
                            } catch (e: Exception) {
                                errors.add("Ligne $lineNumber: ${e.message}")
                            }
                        }
                    }
                } ?: errors.add("Impossible d'ouvrir le fichier")
            } catch (e: SecurityException) {
                errors.add("Permission refusée: ${e.message}")
            } catch (e: Exception) {
                errors.add("Erreur lors de la lecture du fichier: ${e.message}")
            }

            return Pair(transactions, errors)
        }

        /** Détecte le séparateur CSV : point-virgule si la ligne en contient et donne plus de colonnes. */
        private fun detectDelimiter(firstLine: String): Char {
            val byComma = parseCsvLine(firstLine, ',')
            val bySemicolon = parseCsvLine(firstLine, ';')
            return if (bySemicolon.size > byComma.size && bySemicolon.size > 3) ';' else ','
        }

        /** Détecte un en-tête type export banque et retourne le mapping colonne -> index, ou null */
        private fun parseBankHeader(headerLine: String, delimiter: Char = ','): Map<String, Int>? {
            val values = parseCsvLine(headerLine, delimiter)
            val lower = values.map { it.trim().lowercase().replace(" ", "") }
            val dateCol = lower.indexOfFirst { it.contains("bookdate") || it.contains("valuedate") || it == "date" }
            val amountCol = lower.indexOfFirst { it.contains("amount") || it.contains("montant") || it.contains("instructedar") }
            val creditDebitCol = lower.indexOfFirst { it.contains("credit") && it.contains("debit") }
            val creditDebitColAlt = lower.indexOfFirst { it == "credit" || it == "debit" }
            val descCol = lower.indexOfFirst { it.contains("description") || it.contains("libelle") || it.contains("desc") }
            val categoryCol = lower.indexOfFirst { it.contains("category") || it.contains("categorie") }
            val dateIdx = if (dateCol >= 0) dateCol else lower.indexOfFirst { it.contains("date") }
            val amountIdx = if (amountCol >= 0) amountCol else -1
            val typeIdx = if (creditDebitCol >= 0) creditDebitCol else creditDebitColAlt
            val descIdx = if (descCol >= 0) descCol else -1
            if (dateIdx < 0 || amountIdx < 0 || descIdx < 0) return null
            val map = mutableMapOf(
                "date" to dateIdx,
                "amount" to amountIdx,
                "creditDebit" to typeIdx,
                "description" to descIdx
            )
            if (categoryCol >= 0) map["category"] = categoryCol
            return map
        }

        private fun parseBankRow(values: List<String>, header: Map<String, Int>, lineNumber: Int): Transaction? {
            val dateStr = header["date"]?.let { i -> values.getOrNull(i)?.trim() }?.takeIf { it.isNotBlank() } ?: return null
            val amountStr = header["amount"]?.let { i -> values.getOrNull(i)?.trim()?.replace(",", ".")?.replace(" ", "") }?.takeIf { it.isNotBlank() } ?: return null
            val desc = header["description"]?.let { i -> values.getOrNull(i)?.trim()?.replace("\n", " ")?.ifBlank { "Sans libellé" } } ?: "Sans libellé"
            val category = header["category"]?.let { i -> values.getOrNull(i)?.trim()?.ifBlank { "Divers" } } ?: "Divers"
            val date = parseDate(dateStr) ?: throw Exception("Ligne $lineNumber: Date invalide: $dateStr")
            val amountRaw = amountStr.toDoubleOrNull() ?: throw Exception("Ligne $lineNumber: Montant invalide: $amountStr")
            val type = header["creditDebit"]?.let { i -> values.getOrNull(i)?.trim()?.uppercase() }.let { t ->
                when {
                    t == null -> if (amountRaw >= 0) TransactionType.RECETTE else TransactionType.DEPENSE
                    t.contains("CREDIT") -> TransactionType.RECETTE
                    else -> TransactionType.DEPENSE
                }
            }
            val montant = kotlin.math.abs(amountRaw)
            if (montant <= 0) return null
            return Transaction(
                id = 0,
                type = type,
                dateEpoch = date.toEpochDay(),
                libelle = desc,
                objet = "",
                montantTTC = montant,
                categorie = category,
                accountId = 1L
            )
        }

        private fun isHeaderLine(line: String): Boolean {
            val lower = line.lowercase()
            return lower.contains("date") ||
                   lower.contains("type") ||
                   lower.contains("libelle") ||
                   lower.contains("montant")
        }

        private fun parseTransaction(line: String, lineNumber: Int, delimiter: Char = ','): Transaction? {
            val values = parseCsvLine(line, delimiter)
            
            if (values.size < 6) {
                throw Exception("Ligne $lineNumber: Format invalide (${values.size} colonnes, minimum 6 requises)")
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
                ?: throw Exception("Ligne $lineNumber: Format de date invalide: $dateStr")

            // Parse type
            val type = when (typeStr.uppercase()) {
                "RECETTE", "REVENU", "INCOME", "R" -> TransactionType.RECETTE
                "DEPENSE", "EXPENSE", "D", "CHARGE" -> TransactionType.DEPENSE
                else -> throw Exception("Ligne $lineNumber: Type invalide: $typeStr")
            }

            // Parse amount
            val montant = montantStr.toDoubleOrNull()
                ?: throw Exception("Ligne $lineNumber: Montant invalide: $montantStr")

            if (montant <= 0) {
                throw Exception("Ligne $lineNumber: Le montant doit être positif")
            }

            if (libelle.isBlank()) {
                throw Exception("Ligne $lineNumber: Le libellé ne peut pas être vide")
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

        private fun parseCsvLine(line: String, delimiter: Char = ','): List<String> {
            val result = mutableListOf<String>()
            val sb = StringBuilder()
            var inQuotes = false

            for (char in line) {
                when {
                    char == '"' -> inQuotes = !inQuotes
                    char == delimiter && !inQuotes -> {
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
