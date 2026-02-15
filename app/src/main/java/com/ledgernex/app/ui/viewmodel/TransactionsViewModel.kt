package com.ledgernex.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ledgernex.app.data.entity.Transaction
import com.ledgernex.app.data.entity.TransactionType
import com.ledgernex.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

class TransactionsViewModel(
    private val transactionRepo: TransactionRepository
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

    private fun loadTransactions() {
        viewModelScope.launch {
            val year = _state.value.selectedYear
            val month = _state.value.selectedMonth
            val startEpoch = LocalDate.of(year, month, 1).toEpochDay()
            val endEpoch = YearMonth.of(year, month).atEndOfMonth().toEpochDay()

            transactionRepo.getByDateRange(startEpoch, endEpoch).collectLatest { list ->
                _state.value = _state.value.copy(transactions = list, isLoading = false)
            }
        }
    }

    class Factory(
        private val transactionRepo: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransactionsViewModel(transactionRepo) as T
        }
    }
}
