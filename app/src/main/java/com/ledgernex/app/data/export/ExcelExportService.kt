package com.ledgernex.app.data.export

import android.content.Context
import android.os.Environment
import com.ledgernex.app.data.entity.CompanyAccount
import com.ledgernex.app.data.entity.Transaction
import com.ledgernex.app.data.entity.TransactionType
import com.ledgernex.app.data.entity.Asset
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.Locale

class ExcelExportService(private val context: Context) {

    suspend fun exportBilan(
        accounts: List<CompanyAccount>,
        assets: List<Asset>,
        equityAmount: Double,
        currency: String,
        language: String
    ): String {
        val fileName = "LedgerNex_Bilan_${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.xlsx"
        val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LedgerNex")
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet(when (language) {
            "en" -> "Balance Sheet"
            "ar" -> "الميزانية العمومية"
            else -> "Bilan"
        })
        
        // Styles
        val titleStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
            setFont(workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 18
            })
        }
        
        val headerStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply { bold = true })
        }
        
        val currencyStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.RIGHT
            dataFormat = workbook.createDataFormat().getFormat("_($currency* #,##0.00_);_($currency* (#,##0.00);_($currency* \"-\"??_);_(@_)")
        }
        
        val boldStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.RIGHT
            setFont(workbook.createFont().apply { bold = true })
            dataFormat = workbook.createDataFormat().getFormat("_($currency* #,##0.00_);_($currency* (#,##0.00);_($currency* \"-\"??_);_(@_)")
        }
        
        var rowNum = 0
        
        // Title
        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue("${context.getString(com.ledgernex.app.R.string.app_name)} - ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
            cellStyle = titleStyle
        }
        rowNum++
        
        // Assets Section
        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue(when (language) {
                "en" -> "ASSETS"
                "ar" -> "الأصول"
                else -> "ACTIF"
            })
            cellStyle = headerStyle
        }
        
        // Assets Table Headers
        val headerRow = sheet.createRow(rowNum++)
        headerRow.createCell(0).apply {
            setCellValue(when (language) {
                "en" -> "Account"
                "ar" -> "الحساب"
                else -> "Compte"
            })
            cellStyle = headerStyle
        }
        headerRow.createCell(1).apply {
            setCellValue(when (language) {
                "en" -> "Balance"
                "ar" -> "الرصيد"
                else -> "Solde"
            })
            cellStyle = headerStyle
        }
        
        // Assets Data
        val totalAssets = accounts.sumOf { it.soldeInitial }
        accounts.forEach { account ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(account.nom)
            row.createCell(1).apply {
                setCellValue(account.soldeInitial)
                cellStyle = currencyStyle
            }
        }
        
        // Fixed Assets
        val totalAssetsValue = assets.sumOf { it.montantTTC }
        sheet.createRow(rowNum++).apply {
            getCell(0).setCellValue(when (language) {
                "en" -> "Fixed Assets"
                "ar" -> "الأصول الثابتة"
                else -> "Immobilisations"
            })
            getCell(1).apply {
                setCellValue(totalAssetsValue)
                cellStyle = currencyStyle
            }
        }
        
        // Total Assets
        sheet.createRow(rowNum++).apply {
            getCell(0).setCellValue(when (language) {
                "en" -> "TOTAL ASSETS"
                "ar" -> "إجمالي الأصول"
                else -> "TOTAL ACTIF"
            })
            getCell(1).apply {
                setCellValue(totalAssets + totalAssetsValue)
                cellStyle = boldStyle
            }
        }
        
        rowNum++
        
        // Liabilities & Equity Section
        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue(when (language) {
                "en" -> "LIABILITIES & EQUITY"
                "ar" -> "الخصوم وحقوق الملكية"
                else -> "PASSIF"
            })
            cellStyle = headerStyle
        }
        
        // Liabilities Headers
        val liabilitiesHeaderRow = sheet.createRow(rowNum++)
        liabilitiesHeaderRow.createCell(0).apply {
            setCellValue(when (language) {
                "en" -> "Account"
                "ar" -> "الحساب"
                else -> "Compte"
            })
            cellStyle = headerStyle
        }
        liabilitiesHeaderRow.createCell(1).apply {
            setCellValue(when (language) {
                "en" -> "Balance"
                "ar" -> "الرصيد"
                else -> "Solde"
            })
            cellStyle = headerStyle
        }
        
        // Liabilities Data
        val totalLiabilities = accounts.filter { it.soldeInitial < 0 }.sumOf { it.soldeInitial }
        accounts.filter { it.soldeInitial < 0 }.forEach { account ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(account.nom)
            row.createCell(1).apply {
                setCellValue(account.soldeInitial)
                cellStyle = currencyStyle
            }
        }
        
        // Equity
        sheet.createRow(rowNum++).apply {
            getCell(0).setCellValue(when (language) {
                "en" -> "Equity"
                "ar" -> "حقوق الملكية"
                else -> "Capitaux propres"
            })
            getCell(1).apply {
                setCellValue(equityAmount)
                cellStyle = currencyStyle
            }
        }
        
        // Total Liabilities & Equity
        sheet.createRow(rowNum++).apply {
            getCell(0).setCellValue(when (language) {
                "en" -> "TOTAL LIABILITIES & EQUITY"
                "ar" -> "إجمالي الخصوم وحقوق الملكية"
                else -> "TOTAL PASSIF"
            })
            getCell(1).apply {
                setCellValue(totalLiabilities + equityAmount)
                cellStyle = boldStyle
            }
        }
        
        // Auto-size columns
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
        
        // Write to file
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        
        return file.absolutePath
    }

    suspend fun exportCompteResultat(
        transactions: List<Transaction>,
        currency: String,
        language: String,
        period: String
    ): String {
        val fileName = "LedgerNex_Resultat_${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.xlsx"
        val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "LedgerNex")
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet(when (language) {
            "en" -> "Income Statement"
            "ar" -> "قائمة الدخل"
            else -> "Compte de Résultat"
        })
        
        // Styles
        val titleStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
            setFont(workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 18
            })
        }
        
        val headerStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply { bold = true })
        }
        
        val currencyStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.RIGHT
            dataFormat = workbook.createDataFormat().getFormat("_($currency* #,##0.00_);_($currency* (#,##0.00);_($currency* \"-\"??_);_(@_)")
        }
        
        val boldStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.RIGHT
            setFont(workbook.createFont().apply { bold = true })
            dataFormat = workbook.createDataFormat().getFormat("_($currency* #,##0.00_);_($currency* (#,##0.00);_($currency* \"-\"??_);_(@_)")
        }
        
        var rowNum = 0
        
        // Title
        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue("${context.getString(com.ledgernex.app.R.string.app_name)} - $period")
            cellStyle = titleStyle
        }
        rowNum++
        
        // Revenue Section
        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue(when (language) {
                "en" -> "REVENUE"
                "ar" -> "الإيرادات"
                else -> "PRODUITS"
            })
            cellStyle = headerStyle
        }
        
        // Revenue Headers
        val revenueHeaderRow = sheet.createRow(rowNum++)
        revenueHeaderRow.createCell(0).apply {
            setCellValue(when (language) {
                "en" -> "Description"
                "ar" -> "الوصف"
                else -> "Description"
            })
            cellStyle = headerStyle
        }
        revenueHeaderRow.createCell(1).apply {
            setCellValue(when (language) {
                "en" -> "Amount"
                "ar" -> "المبلغ"
                else -> "Montant"
            })
            cellStyle = headerStyle
        }
        
        // Revenue Data
        val revenues = transactions.filter { it.type == TransactionType.RECETTE }
        val totalRevenue = revenues.sumOf { it.montantTTC }
        
        revenues.forEach { transaction ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(transaction.libelle)
            row.createCell(1).apply {
                setCellValue(transaction.montantTTC)
                cellStyle = currencyStyle
            }
        }
        
        // Total Revenue
        sheet.createRow(rowNum++).apply {
            getCell(0).setCellValue(when (language) {
                "en" -> "TOTAL REVENUE"
                "ar" -> "إجمالي الإيرادات"
                else -> "TOTAL PRODUITS"
            })
            getCell(1).apply {
                setCellValue(totalRevenue)
                cellStyle = boldStyle
            }
        }
        
        rowNum++
        
        // Expenses Section
        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue(when (language) {
                "en" -> "EXPENSES"
                "ar" -> "المصروفات"
                else -> "CHARGES"
            })
            cellStyle = headerStyle
        }
        
        // Expenses Headers
        val expenseHeaderRow = sheet.createRow(rowNum++)
        expenseHeaderRow.createCell(0).apply {
            setCellValue(when (language) {
                "en" -> "Description"
                "ar" -> "الوصف"
                else -> "Description"
            })
            cellStyle = headerStyle
        }
        expenseHeaderRow.createCell(1).apply {
            setCellValue(when (language) {
                "en" -> "Amount"
                "ar" -> "المبلغ"
                else -> "Montant"
            })
            cellStyle = headerStyle
        }
        
        // Expenses Data
        val expenses = transactions.filter { it.type == TransactionType.DEPENSE }
        val totalExpenses = expenses.sumOf { it.montantTTC }
        
        expenses.forEach { transaction ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(transaction.libelle)
            row.createCell(1).apply {
                setCellValue(transaction.montantTTC)
                cellStyle = currencyStyle
            }
        }
        
        // Total Expenses
        sheet.createRow(rowNum++).apply {
            getCell(0).setCellValue(when (language) {
                "en" -> "TOTAL EXPENSES"
                "ar" -> "إجمالي المصروفات"
                else -> "TOTAL CHARGES"
            })
            getCell(1).apply {
                setCellValue(totalExpenses)
                cellStyle = boldStyle
            }
        }
        
        rowNum++
        
        // Net Result
        val result = totalRevenue - totalExpenses
        sheet.createRow(rowNum++).createCell(0).apply {
            setCellValue(when (language) {
                "en" -> "NET RESULT"
                "ar" -> "صافي النتيجة"
                else -> "RÉSULTAT NET"
            })
            cellStyle = headerStyle
        }
        sheet.createRow(rowNum++).createCell(1).apply {
            setCellValue(result)
            cellStyle = boldStyle
        }
        
        // Auto-size columns
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
        
        // Write to file
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        
        return file.absolutePath
    }
}
