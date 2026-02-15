package com.ledgernex.app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ledgernex.app.data.export.ExcelExportService
import com.ledgernex.app.data.export.PdfExportService
import com.ledgernex.app.domain.repository.AccountRepository
import com.ledgernex.app.domain.repository.AssetRepository
import com.ledgernex.app.domain.repository.TransactionRepository
import com.ledgernex.app.data.entity.AccountWithBalance
import com.ledgernex.app.data.entity.CompanyAccount
import com.ledgernex.app.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ExportState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class ExportViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val assetRepository: AssetRepository,
    private val settingsDataStore: SettingsDataStore,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ExportState())
    val state: StateFlow<ExportState> = _state.asStateFlow()

    private val pdfExportService = PdfExportService(context)
    private val excelExportService = ExcelExportService(context)

    fun exportBilanPdf() {
        viewModelScope.launch {
            _state.value = ExportState(isLoading = true)
            try {
                val accountsWithBalance = accountRepository.getAllAccountsWithBalance()
                val assets = assetRepository.getAllAssets()
                val equityAmount = settingsDataStore.equityAmount.first()
                val currency = settingsDataStore.currency.first()
                val language = settingsDataStore.language.first()

                // Convert AccountWithBalance to CompanyAccount with balance
                val accounts = accountsWithBalance.map { accountWithBalance ->
                    CompanyAccount(
                        id = accountWithBalance.id,
                        nom = accountWithBalance.nom,
                        soldeInitial = accountWithBalance.balance, // Use calculated balance
                        actif = accountWithBalance.actif,
                        type = accountWithBalance.type
                    )
                }

                val filePath = pdfExportService.exportBilan(accounts, assets, equityAmount, currency, language)
                _state.value = ExportState(
                    isLoading = false,
                    successMessage = "PDF exporté vers: $filePath"
                )
            } catch (e: Exception) {
                _state.value = ExportState(
                    isLoading = false,
                    errorMessage = "Erreur export PDF: ${e.message}"
                )
            }
        }
    }

    fun exportBilanExcel() {
        viewModelScope.launch {
            _state.value = ExportState(isLoading = true)
            try {
                val accountsWithBalance = accountRepository.getAllAccountsWithBalance()
                val assets = assetRepository.getAllAssets()
                val equityAmount = settingsDataStore.equityAmount.first()
                val currency = settingsDataStore.currency.first()
                val language = settingsDataStore.language.first()

                // Convert AccountWithBalance to CompanyAccount with balance
                val accounts = accountsWithBalance.map { accountWithBalance ->
                    CompanyAccount(
                        id = accountWithBalance.id,
                        nom = accountWithBalance.nom,
                        soldeInitial = accountWithBalance.balance, // Use calculated balance
                        actif = accountWithBalance.actif,
                        type = accountWithBalance.type
                    )
                }

                val filePath = excelExportService.exportBilan(accounts, assets, equityAmount, currency, language)
                _state.value = ExportState(
                    isLoading = false,
                    successMessage = "Excel exporté vers: $filePath"
                )
            } catch (e: Exception) {
                _state.value = ExportState(
                    isLoading = false,
                    errorMessage = "Erreur export Excel: ${e.message}"
                )
            }
        }
    }

    fun exportCompteResultatPdf(period: String = "Mois en cours") {
        viewModelScope.launch {
            _state.value = ExportState(isLoading = true)
            try {
                val today = LocalDate.now()
                val startOfMonth = today.withDayOfMonth(1)
                val transactions = transactionRepository.getTransactionsByDateRange(startOfMonth.toEpochDay(), today.toEpochDay())
                val currency = settingsDataStore.currency.first()
                val language = settingsDataStore.language.first()

                val filePath = pdfExportService.exportCompteResultat(transactions, currency, language, period)
                _state.value = ExportState(
                    isLoading = false,
                    successMessage = "PDF exporté vers: $filePath"
                )
            } catch (e: Exception) {
                _state.value = ExportState(
                    isLoading = false,
                    errorMessage = "Erreur export PDF: ${e.message}"
                )
            }
        }
    }

    fun exportCompteResultatExcel(period: String = "Mois en cours") {
        viewModelScope.launch {
            _state.value = ExportState(isLoading = true)
            try {
                val today = LocalDate.now()
                val startOfMonth = today.withDayOfMonth(1)
                val transactions = transactionRepository.getTransactionsByDateRange(startOfMonth.toEpochDay(), today.toEpochDay())
                val currency = settingsDataStore.currency.first()
                val language = settingsDataStore.language.first()

                val filePath = excelExportService.exportCompteResultat(transactions, currency, language, period)
                _state.value = ExportState(
                    isLoading = false,
                    successMessage = "Excel exporté vers: $filePath"
                )
            } catch (e: Exception) {
                _state.value = ExportState(
                    isLoading = false,
                    errorMessage = "Erreur export Excel: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }

    class Factory(
        private val accountRepository: AccountRepository,
        private val transactionRepository: TransactionRepository,
        private val assetRepository: AssetRepository,
        private val settingsDataStore: SettingsDataStore,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExportViewModel::class.java)) {
                return ExportViewModel(
                    accountRepository,
                    transactionRepository,
                    assetRepository,
                    settingsDataStore,
                    context
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
