package com.ledgernex.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ledgernex.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class ResultatState(
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue,
    // Vue mensuelle
    val totalProduitsMois: Double = 0.0,
    val totalChargesMois: Double = 0.0,
    val resultatMois: Double = 0.0,
    // Vue annuelle
    val totalProduitsAnnuel: Double = 0.0,
    val totalChargesAnnuel: Double = 0.0,
    val resultatAnnuel: Double = 0.0,
    val margePercent: Double = 0.0,
    val isLoading: Boolean = true
)

class ResultatViewModel(
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ResultatState())
    val state: StateFlow<ResultatState> = _state

    init {
        loadResultat()
    }

    fun setMonth(year: Int, month: Int) {
        _state.value = _state.value.copy(selectedYear = year, selectedMonth = month)
        loadResultat()
    }

    private fun loadResultat() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val year = _state.value.selectedYear
            val month = _state.value.selectedMonth

            // Mensuel
            val monthStart = LocalDate.of(year, month, 1).toEpochDay()
            val monthEnd = YearMonth.of(year, month).atEndOfMonth().toEpochDay()
            val produitsMois = transactionRepo.getTotalProduits(monthStart, monthEnd)
            val chargesMois = transactionRepo.getTotalCharges(monthStart, monthEnd)
            val resultatMois = produitsMois - chargesMois

            // Annuel
            val yearStart = LocalDate.of(year, 1, 1).toEpochDay()
            val yearEnd = LocalDate.of(year, 12, 31).toEpochDay()
            val produitsAnnuel = transactionRepo.getTotalProduits(yearStart, yearEnd)
            val chargesAnnuel = transactionRepo.getTotalCharges(yearStart, yearEnd)
            val resultatAnnuel = produitsAnnuel - chargesAnnuel
            val marge = if (produitsAnnuel > 0) (resultatAnnuel / produitsAnnuel) * 100 else 0.0

            _state.value = ResultatState(
                selectedYear = year,
                selectedMonth = month,
                totalProduitsMois = produitsMois,
                totalChargesMois = chargesMois,
                resultatMois = resultatMois,
                totalProduitsAnnuel = produitsAnnuel,
                totalChargesAnnuel = chargesAnnuel,
                resultatAnnuel = resultatAnnuel,
                margePercent = marge,
                isLoading = false
            )
        }
    }

    class Factory(
        private val transactionRepo: TransactionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ResultatViewModel(transactionRepo) as T
        }
    }
}
