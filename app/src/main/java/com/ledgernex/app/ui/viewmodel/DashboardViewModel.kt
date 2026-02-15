package com.ledgernex.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ledgernex.app.data.entity.Asset
import com.ledgernex.app.domain.repository.AccountRepository
import com.ledgernex.app.domain.repository.AssetRepository
import com.ledgernex.app.domain.repository.TransactionRepository
import com.ledgernex.app.manager.RecurrenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth

data class DashboardState(
    val soldeTotalEntreprise: Double = 0.0,
    val resultatMois: Double = 0.0,
    val resultatAnnuel: Double = 0.0,
    val valeurImmobilisations: Double = 0.0,
    val totalActif: Double = 0.0,
    val totalPassif: Double = 0.0,
    val monthlyResults: List<Pair<String, Double>> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository,
    private val assetRepo: AssetRepository,
    private val recurrenceManager: RecurrenceManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    init {
        viewModelScope.launch {
            // Générer les récurrences en attente au démarrage
            recurrenceManager.generatePendingTransactions()
            loadDashboard()
        }
    }

    fun refresh() {
        viewModelScope.launch { loadDashboard() }
    }

    private suspend fun loadDashboard() {
        _state.value = _state.value.copy(isLoading = true)

        val today = LocalDate.now()
        val year = today.year
        val month = today.monthValue

        // Solde total entreprise
        val accounts = accountRepo.getActiveAccounts().first()
        var soldeTotalEntreprise = 0.0
        for (account in accounts) {
            soldeTotalEntreprise += accountRepo.getAccountBalance(account.id)
        }

        // Résultat du mois
        val monthStart = LocalDate.of(year, month, 1).toEpochDay()
        val monthEnd = YearMonth.of(year, month).atEndOfMonth().toEpochDay()
        val resultatMois = transactionRepo.getResultForPeriod(monthStart, monthEnd)

        // Résultat annuel
        val yearStart = LocalDate.of(year, 1, 1).toEpochDay()
        val yearEnd = LocalDate.of(year, 12, 31).toEpochDay()
        val resultatAnnuel = transactionRepo.getResultForPeriod(yearStart, yearEnd)

        // Valeur immobilisations
        val assets = assetRepo.getAll().first()
        val valeurImmo = assets.sumOf { calculateNetAssetValue(it) }

        // Total actif / passif simplifié pour le résumé
        val totalActif = soldeTotalEntreprise + valeurImmo

        // Résultats mensuels pour graphique (12 mois de l'année en cours)
        val monthlyResults = mutableListOf<Pair<String, Double>>()
        for (m in 1..12) {
            val mStart = LocalDate.of(year, m, 1).toEpochDay()
            val mEnd = YearMonth.of(year, m).atEndOfMonth().toEpochDay()
            val result = transactionRepo.getResultForPeriod(mStart, mEnd)
            val label = YearMonth.of(year, m).month.name.take(3)
            monthlyResults.add(label to result)
        }

        _state.value = DashboardState(
            soldeTotalEntreprise = soldeTotalEntreprise,
            resultatMois = resultatMois,
            resultatAnnuel = resultatAnnuel,
            valeurImmobilisations = valeurImmo,
            totalActif = totalActif,
            totalPassif = 0.0, // sera calculé avec equity dans BilanViewModel
            monthlyResults = monthlyResults,
            isLoading = false
        )
    }

    companion object {
        fun calculateNetAssetValue(asset: Asset): Double {
            val yearsElapsed = Period.between(
                LocalDate.ofEpochDay(asset.dateAchatEpoch),
                LocalDate.now()
            ).years
            val amortissement = (asset.montantTTC / asset.dureeAmortissement) * yearsElapsed
            return (asset.montantTTC - amortissement).coerceAtLeast(0.0)
        }
    }

    class Factory(
        private val accountRepo: AccountRepository,
        private val transactionRepo: TransactionRepository,
        private val assetRepo: AssetRepository,
        private val recurrenceManager: RecurrenceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(accountRepo, transactionRepo, assetRepo, recurrenceManager) as T
        }
    }
}
