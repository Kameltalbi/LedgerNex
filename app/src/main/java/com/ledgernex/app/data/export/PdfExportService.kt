package com.ledgernex.app.data.export

import android.content.Context
import android.os.Environment
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.ledgernex.app.data.entity.CompanyAccount
import com.ledgernex.app.data.entity.Transaction
import com.ledgernex.app.data.entity.TransactionType
import com.ledgernex.app.data.entity.Asset
import com.ledgernex.app.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.Locale

class PdfExportService(private val context: Context) {

    suspend fun exportBilan(
        accounts: List<CompanyAccount>,
        assets: List<Asset>,
        equityAmount: Double,
        currency: String,
        language: String
    ): String {
        val fileName = "LedgerNex_Bilan_${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.pdf"
        val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LedgerNex")
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        
        val writer = PdfWriter(FileOutputStream(file))
        val pdf = PdfDocument(writer)
        val document = Document(pdf)
        
        // Title
        val title = when (language) {
            "en" -> "Balance Sheet"
            "ar" -> "الميزانية العمومية"
            else -> "Bilan"
        }
        document.add(Paragraph(title).setFontSize(18f).setBold().setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph("${context.getString(com.ledgernex.app.R.string.app_name)} - ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}").setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph(" "))
        
        // Assets
        document.add(Paragraph(when (language) {
            "en" -> "ASSETS"
            "ar" -> "الأصول"
            else -> "ACTIF"
        }).setFontSize(14f).setBold())
        
        val assetsTable = Table(UnitValue.createPercentArray(floatArrayOf(60f, 40f))).useAllAvailableWidth()
        assetsTable.addHeaderCell(when (language) {
            "en" -> "Account"
            "ar" -> "الحساب"
            else -> "Compte"
        })
        assetsTable.addHeaderCell(when (language) {
            "en" -> "Balance"
            "ar" -> "الرصيد"
            else -> "Solde"
        })
        
        val totalAssets = accounts.sumOf { it.soldeInitial }
        accounts.forEach { account ->
            assetsTable.addCell(account.nom)
            assetsTable.addCell(formatCurrency(account.soldeInitial, currency))
        }
        
        // Add assets value
        val totalAssetsValue = assets.sumOf { it.montantTTC }
        assetsTable.addCell(when (language) {
            "en" -> "Fixed Assets"
            "ar" -> "الأصول الثابتة"
            else -> "Immobilisations"
        })
        assetsTable.addCell(formatCurrency(totalAssetsValue, currency))
        
        assetsTable.addCell(when (language) {
            "en" -> "TOTAL ASSETS"
            "ar" -> "إجمالي الأصول"
            else -> "TOTAL ACTIF"
        })
        assetsTable.addCell(formatCurrency(totalAssets + totalAssetsValue, currency))
        
        document.add(assetsTable)
        document.add(Paragraph(" "))
        
        // Liabilities & Equity
        document.add(Paragraph(when (language) {
            "en" -> "LIABILITIES & EQUITY"
            "ar" -> "الخصوم وحقوق الملكية"
            else -> "PASSIF"
        }).setFontSize(14f).setBold())
        
        val liabilitiesTable = Table(UnitValue.createPercentArray(floatArrayOf(60f, 40f))).useAllAvailableWidth()
        liabilitiesTable.addHeaderCell(when (language) {
            "en" -> "Account"
            "ar" -> "الحساب"
            else -> "Compte"
        })
        liabilitiesTable.addHeaderCell(when (language) {
            "en" -> "Balance"
            "ar" -> "الرصيد"
            else -> "Solde"
        })
        
        val totalLiabilities = accounts.filter { it.soldeInitial < 0 }.sumOf { it.soldeInitial }
        accounts.filter { it.soldeInitial < 0 }.forEach { account ->
            liabilitiesTable.addCell(account.nom)
            liabilitiesTable.addCell(formatCurrency(account.soldeInitial, currency))
        }
        
        liabilitiesTable.addCell(when (language) {
            "en" -> "Equity"
            "ar" -> "حقوق الملكية"
            else -> "Capitaux propres"
        })
        liabilitiesTable.addCell(formatCurrency(equityAmount, currency))
        
        liabilitiesTable.addCell(when (language) {
            "en" -> "TOTAL LIABILITIES & EQUITY"
            "ar" -> "إجمالي الخصوم وحقوق الملكية"
            else -> "TOTAL PASSIF"
        })
        liabilitiesTable.addCell(formatCurrency(totalLiabilities + equityAmount, currency))
        
        document.add(liabilitiesTable)
        document.close()
        
        return file.absolutePath
    }

    suspend fun exportCompteResultat(
        transactions: List<Transaction>,
        currency: String,
        language: String,
        period: String
    ): String {
        val fileName = "LedgerNex_Resultat_${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.pdf"
        val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LedgerNex")
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        
        val writer = PdfWriter(FileOutputStream(file))
        val pdf = PdfDocument(writer)
        val document = Document(pdf)
        
        // Title
        val title = when (language) {
            "en" -> "Income Statement"
            "ar" -> "قائمة الدخل"
            else -> "Compte de Résultat"
        }
        document.add(Paragraph(title).setFontSize(18f).setBold().setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph("${context.getString(com.ledgernex.app.R.string.app_name)} - $period").setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph(" "))
        
        // Revenue
        val revenues = transactions.filter { it.type == TransactionType.RECETTE }
        val totalRevenue = revenues.sumOf { it.montantTTC }
        
        document.add(Paragraph(when (language) {
            "en" -> "REVENUE"
            "ar" -> "الإيرادات"
            else -> "PRODUITS"
        }).setFontSize(14f).setBold())
        
        val revenueTable = Table(UnitValue.createPercentArray(floatArrayOf(60f, 40f))).useAllAvailableWidth()
        revenueTable.addHeaderCell(when (language) {
            "en" -> "Description"
            "ar" -> "الوصف"
            else -> "Description"
        })
        revenueTable.addHeaderCell(when (language) {
            "en" -> "Amount"
            "ar" -> "المبلغ"
            else -> "Montant"
        })
        
        revenues.forEach { transaction ->
            revenueTable.addCell(transaction.libelle)
            revenueTable.addCell(formatCurrency(transaction.montantTTC, currency))
        }
        
        revenueTable.addCell(when (language) {
            "en" -> "TOTAL REVENUE"
            "ar" -> "إجمالي الإيرادات"
            else -> "TOTAL PRODUITS"
        })
        revenueTable.addCell(formatCurrency(totalRevenue, currency))
        
        document.add(revenueTable)
        document.add(Paragraph(" "))
        
        // Expenses
        val expenses = transactions.filter { it.type == TransactionType.DEPENSE }
        val totalExpenses = expenses.sumOf { it.montantTTC }
        
        document.add(Paragraph(when (language) {
            "en" -> "EXPENSES"
            "ar" -> "المصروفات"
            else -> "CHARGES"
        }).setFontSize(14f).setBold())
        
        val expenseTable = Table(UnitValue.createPercentArray(floatArrayOf(60f, 40f))).useAllAvailableWidth()
        expenseTable.addHeaderCell(when (language) {
            "en" -> "Description"
            "ar" -> "الوصف"
            else -> "Description"
        })
        expenseTable.addHeaderCell(when (language) {
            "en" -> "Amount"
            "ar" -> "المبلغ"
            else -> "Montant"
        })
        
        expenses.forEach { transaction ->
            expenseTable.addCell(transaction.libelle)
            expenseTable.addCell(formatCurrency(transaction.montantTTC, currency))
        }
        
        expenseTable.addCell(when (language) {
            "en" -> "TOTAL EXPENSES"
            "ar" -> "إجمالي المصروفات"
            else -> "TOTAL CHARGES"
        })
        expenseTable.addCell(formatCurrency(totalExpenses, currency))
        
        document.add(expenseTable)
        document.add(Paragraph(" "))
        
        // Result
        val result = totalRevenue - totalExpenses
        document.add(Paragraph(when (language) {
            "en" -> "NET RESULT"
            "ar" -> "صافي النتيجة"
            else -> "RÉSULTAT NET"
        }).setFontSize(14f).setBold())
        document.add(Paragraph(formatCurrency(result, currency)).setFontSize(16f).setBold().setTextAlignment(TextAlignment.CENTER))
        
        document.close()
        
        return file.absolutePath
    }

    private fun formatCurrency(amount: Double, currency: String): String {
        val locale = when (currency) {
            "EUR" -> Locale.FRANCE
            "USD" -> Locale.US
            "GBP" -> Locale.UK
            "TND", "DZD", "MAD" -> Locale.FRANCE
            else -> Locale.FRANCE
        }
        val format = NumberFormat.getCurrencyInstance(locale)
        val formatted = format.format(amount)
        // Simple approach: just prepend currency symbol
        return "$currency $formatted"
    }
}
