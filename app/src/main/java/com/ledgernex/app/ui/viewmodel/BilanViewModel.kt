package com.ledgernex.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ledgernex.app.data.datastore.SettingsDataStore
import com.ledgernex.app.data.entity.Asset
import com.ledgernex.app.domain.repository.AccountRepository
import com.ledgernex.app.domain.repository.AssetRepository
import com.ledgernex.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import kotlin.math.abs

data class BilanState(
    // ACTIF
    val tresorerieTotale: Double = 0.0,
    val valeurNetteImmobilisations: Double = 0.0,
    val totalActif: Double = 0.0,
    // PASSIF
    val capitauxPropres: Double = 0.0,
    val resultatExercice: Double = 0.0,
    val totalPassif: Double = 0.0,
    // Équilibre
    val isBalanced: Boolean = true,
    val isLoading: Boolean = true
)

class BilanViewModel(
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository,
    private val assetRepo: AssetRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(BilanState())
    val state: StateFlow<BilanState> = _state

    init {
        loadBilan()
    }

    fun refresh() {
        loadBilan()
    }

    fun setCapitauxPropres(amount: Double) {
        viewModelScope.launch {
            settingsDataStore.setEquityAmount(amount)
            loadBilan()
        }
    }

    private fun loadBilan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // ACTIF : Trésorerie totale
            val accounts = accountRepo.getActiveAccounts().first()
            var tresorerie = 0.0
            for (account in accounts) {
                tresorerie += accountRepo.getAccountBalance(account.id)
            }

            // ACTIF : Valeur nette immobilisations
            val assets = assetRepo.getAll().first()
            val valeurImmo = assets.sumOf { calculateNetAssetValue(it) }

            val totalActif = tresorerie + valeurImmo

            // PASSIF : Capitaux propres (DataStore)
            val capitauxPropres = settingsDataStore.equityAmount.first()

            // PASSIF : Résultat exercice (année en cours)
            val year = LocalDate.now().year
            val yearStart = LocalDate.of(year, 1, 1).toEpochDay()
            val yearEnd = LocalDate.of(year, 12, 31).toEpochDay()
            val resultatExercice = transactionRepo.getResultForPeriod(yearStart, yearEnd)

            val totalPassif = capitauxPropres + resultatExercice

            // Équilibre avec tolérance
            val isBalanced = abs(totalActif - totalPassif) < 0.01

            _state.value = BilanState(
                tresorerieTotale = tresorerie,
                valeurNetteImmobilisations = valeurImmo,
                totalActif = totalActif,
                capitauxPropres = capitauxPropres,
                resultatExercice = resultatExercice,
                totalPassif = totalPassif,
                isBalanced = isBalanced,
                isLoading = false
            )
        }
    }

    private fun calculateNetAssetValue(asset: Asset): Double {
        val yearsElapsed = Period.between(
            LocalDate.ofEpochDay(asset.dateAchatEpoch),
            LocalDate.now()
        ).years
        val amortissement = (asset.montantTTC / asset.dureeAmortissement) * yearsElapsed
        return (asset.montantTTC - amortissement).coerceAtLeast(0.0)
    }

    class Factory(
        private val accountRepo: AccountRepository,
        private val transactionRepo: TransactionRepository,
        private val assetRepo: AssetRepository,
        private val settingsDataStore: SettingsDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BilanViewModel(accountRepo, transactionRepo, assetRepo, settingsDataStore) as T
        }
    }
}
