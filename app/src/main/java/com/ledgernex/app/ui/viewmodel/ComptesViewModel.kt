package com.ledgernex.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ledgernex.app.data.entity.AccountType
import com.ledgernex.app.data.entity.CompanyAccount
import com.ledgernex.app.data.entity.Transaction
import com.ledgernex.app.domain.repository.AccountRepository
import com.ledgernex.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AccountWithBalance(
    val account: CompanyAccount,
    val solde: Double,
    val totalRecettes: Double = 0.0,
    val totalDepenses: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList()
)

data class ComptesState(
    val accounts: List<AccountWithBalance> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ComptesViewModel(
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ComptesState())
    val state: StateFlow<ComptesState> = _state

    init {
        loadAccounts()
    }

    fun addAccount(nom: String, type: AccountType, soldeInitial: Double) {
        viewModelScope.launch {
            try {
                accountRepo.insert(
                    CompanyAccount(nom = nom, type = type, soldeInitial = soldeInitial)
                )
                _state.value = _state.value.copy(error = null)
            } catch (e: IllegalStateException) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun updateAccount(account: CompanyAccount) {
        viewModelScope.launch {
            try {
                accountRepo.update(account)
                _state.value = _state.value.copy(error = null)
            } catch (e: IllegalStateException) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun deleteAccount(account: CompanyAccount) {
        viewModelScope.launch {
            try {
                accountRepo.delete(account)
                _state.value = _state.value.copy(error = null)
            } catch (e: IllegalStateException) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepo.getAll().collectLatest { accounts ->
                val withBalances = accounts.map { account ->
                    AccountWithBalance(
                        account = account,
                        solde = accountRepo.getAccountBalance(account.id),
                        totalRecettes = transactionRepo.getTotalRecettesForAccount(account.id),
                        totalDepenses = transactionRepo.getTotalDepensesForAccount(account.id),
                        recentTransactions = transactionRepo.getRecentByAccount(account.id, 5)
                    )
                }
                _state.value = ComptesState(accounts = withBalances, isLoading = false)
            }
        }
    }

    class Factory(
        private val accountRepo: AccountRepository,
        private val transactionRepo: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ComptesViewModel(accountRepo, transactionRepo) as T
        }
    }
}
