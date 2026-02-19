package com.ledgernex.app.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.ledgernex.app.data.entity.Transaction
import com.ledgernex.app.data.entity.TransactionType
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Service for exporting transactions to CSV format
 */
class CsvExportService(private val context: Context) {

    /**
     * Export transactions to CSV file and return the file path
     */
    fun exportTransactions(
        transactions: List<Transaction>,
        fileName: String = "transactions_export_${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.csv"
    ): String {
        val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LedgerNex")
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        
        val file = File(downloadsDir, fileName)
        
        FileWriter(file).use { writer ->
            // Header
            writer.append("date,type,libelle,objet,montant,categorie,accountId\n")
            
            // Data rows
            transactions.forEach { tx ->
                val date = LocalDate.ofEpochDay(tx.dateEpoch).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                val type = if (tx.type == TransactionType.RECETTE) "RECETTE" else "DEPENSE"
                val libelle = escapeCsv(tx.libelle)
                val objet = escapeCsv(tx.objet)
                val montant = String.format("%.2f", tx.montantTTC).replace(".", ",")
                val categorie = escapeCsv(tx.categorie)
                
                writer.append("$date,$type,$libelle,$objet,$montant,$categorie,${tx.accountId}\n")
            }
        }
        
        return file.absolutePath
    }
    
    /**
     * Export transactions and create a shareable URI
     */
    fun exportAndShare(
        transactions: List<Transaction>,
        fileName: String = "transactions_export_${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.csv"
    ): Uri {
        val filePath = exportTransactions(transactions, fileName)
        val file = File(filePath)
        
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
    
    /**
     * Create share intent for the exported CSV
     */
    fun createShareIntent(uri: Uri): Intent {
        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
