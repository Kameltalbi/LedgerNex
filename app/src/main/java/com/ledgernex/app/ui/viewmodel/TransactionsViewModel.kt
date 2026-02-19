package com.ledgernex.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ledgernex.app.data.entity.AccountType
import com.ledgernex.app.data.entity.CompanyAccount
import com.ledgernex.app.data.entity.Transaction
import com.ledgernex.app.data.entity.TransactionType
import com.ledgernex.app.domain.repository.AccountRepository
import com.ledgernex.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val searchQuery: String = "",
    val filterAccountId: Long? = null,
    val filterCategorie: String? = null,
    val isLoading: Boolean = true,
    val selectedTransactionIds: Set<Long> = emptySet()
)

class TransactionsViewModel(
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionsState())
    val state: StateFlow<TransactionsState> = _state

    init {
        loadTransactions()
    }

    fun setMonth(year: Int, month: Int) {
        _state.value = _state.value.copy(selectedYear = year, selectedMonth = month, searchQuery = "")
        loadTransactions()
    }

    fun setFilterAccount(accountId: Long?) {
        _state.value = _state.value.copy(filterAccountId = accountId)
        loadTransactions()
    }

    fun setFilterCategorie(categorie: String?) {
        _state.value = _state.value.copy(filterCategorie = categorie)
        loadTransactions()
    }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        if (query.isBlank()) {
            loadTransactions()
        } else {
            viewModelScope.launch {
                transactionRepo.search(query).collectLatest { list ->
                    _state.value = _state.value.copy(transactions = list, isLoading = false)
                }
            }
        }
    }

    fun toggleTransactionSelection(transactionId: Long) {
        val currentSelected = _state.value.selectedTransactionIds
        _state.value = _state.value.copy(
            selectedTransactionIds = if (currentSelected.contains(transactionId)) {
                currentSelected - transactionId
            } else {
                currentSelected + transactionId
            }
        )
    }

    fun selectAllTransactions() {
        _state.value = _state.value.copy(
            selectedTransactionIds = _state.value.transactions.map { it.id }.toSet()
        )
    }

    fun deselectAllTransactions() {
        _state.value = _state.value.copy(selectedTransactionIds = emptySet())
    }

    fun deleteSelectedTransactions() {
        viewModelScope.launch {
            val selectedIds = _state.value.selectedTransactionIds
            val transactionsToDelete = _state.value.transactions.filter { it.id in selectedIds }
            transactionsToDelete.forEach { transactionRepo.delete(it) }
            _state.value = _state.value.copy(selectedTransactionIds = emptySet())
            loadTransactions()
        }
    }

    fun addTransaction(
        type: TransactionType,
        date: LocalDate,
        libelle: String,
        objet: String,
        montantTTC: Double,
        categorie: String,
        accountId: Long
    ) {
        viewModelScope.launch {
            transactionRepo.insert(
                Transaction(
                    type = type,
                    dateEpoch = date.toEpochDay(),
                    libelle = libelle,
                    objet = objet,
                    montantTTC = montantTTC,
                    categorie = categorie,
                    accountId = accountId
                )
            )
            loadTransactions()
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepo.update(transaction)
            loadTransactions()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepo.delete(transaction)
            loadTransactions()
        }
    }

    suspend fun importTransactions(transactions: List<Transaction>): Pair<Int, List<String>> {
        val errors = mutableListOf<String>()
        var successCount = 0
        var duplicateCount = 0
        
        // Get existing transactions to check for duplicates
        val existingTransactions = transactionRepo.getAll().first()
        val existingKeys = existingTransactions.map { 
            "${it.dateEpoch}-${it.montantTTC}-${it.libelle}-${it.accountId}"
        }.toSet()
        
        // Create missing accounts and map old IDs to new IDs
        val accountIdMapping = mutableMapOf<Long, Long>()
        accountRepo?.let { repo ->
            val existingAccounts = repo.getAllAccounts().map { it.id to it }.toMap()
            val requiredAccountIds = transactions.map { it.accountId }.toSet()
            
            requiredAccountIds.forEach { oldAccountId ->
                if (existingAccounts.containsKey(oldAccountId)) {
                    accountIdMapping[oldAccountId] = oldAccountId
                } else {
                    try {
                        val newAccount = CompanyAccount(
                            id = 0,
                            nom = "Compte Import $oldAccountId",
                            soldeInitial = 0.0,
                            actif = true,
                            type = AccountType.BANK
                        )
                        val newId = repo.insert(newAccount)
                        accountIdMapping[oldAccountId] = newId
                    } catch (e: Exception) {
                        errors.add("Erreur création compte $oldAccountId: ${e.message}")
                        accountIdMapping[oldAccountId] = 1L
                    }
                }
            }
        }
        
        // Import transactions with mapped account IDs, skipping duplicates
        transactions.forEach { transaction ->
            try {
                val mappedAccountId = accountIdMapping[transaction.accountId] ?: transaction.accountId
                val transactionKey = "${transaction.dateEpoch}-${transaction.montantTTC}-${transaction.libelle}-$mappedAccountId"
                
                if (existingKeys.contains(transactionKey)) {
                    duplicateCount++
                } else {
                    val adjustedTransaction = transaction.copy(
                        id = 0,
                        accountId = mappedAccountId
                    )
                    transactionRepo.insert(adjustedTransaction)
                    successCount++
                }
            } catch (e: Exception) {
                errors.add("Erreur insertion: ${e.message}")
            }
        }
        
        if (duplicateCount > 0) {
            errors.add("$duplicateCount transaction(s) ignorée(s) (doublons)")
        }
        
        loadTransactions()
        return Pair(successCount, errors)
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            val year = _state.value.selectedYear
            val month = _state.value.selectedMonth
            val startEpoch = LocalDate.of(year, month, 1).toEpochDay()
            val endEpoch = YearMonth.of(year, month).atEndOfMonth().toEpochDay()
            val accountId = _state.value.filterAccountId
            val categorie = _state.value.filterCategorie

            val flow = when {
                accountId != null && categorie != null ->
                    transactionRepo.getByDateRangeAccountAndCategory(startEpoch, endEpoch, accountId, categorie)
                accountId != null ->
                    transactionRepo.getByDateRangeAndAccount(startEpoch, endEpoch, accountId)
                categorie != null ->
                    transactionRepo.getByDateRangeAndCategory(startEpoch, endEpoch, categorie)
                else ->
                    transactionRepo.getByDateRange(startEpoch, endEpoch)
            }

            flow.collectLatest { list ->
                _state.value = _state.value.copy(transactions = list, isLoading = false)
            }
        }
    }

    class Factory(
        private val transactionRepo: TransactionRepository,
        private val accountRepo: AccountRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransactionsViewModel(transactionRepo, accountRepo) as T
        }
    }
}
