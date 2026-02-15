package com.ledgernex.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ledgernex.app.data.entity.Asset
import com.ledgernex.app.domain.repository.AssetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period

data class AssetDetail(
    val asset: Asset,
    val amortissementAnnuel: Double,
    val amortissementCumule: Double,
    val valeurNetteComptable: Double
)

data class AssetsState(
    val assets: List<AssetDetail> = emptyList(),
    val totalValeurNette: Double = 0.0,
    val isLoading: Boolean = true
)

class AssetsViewModel(
    private val assetRepo: AssetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AssetsState())
    val state: StateFlow<AssetsState> = _state

    init {
        loadAssets()
    }

    fun addAsset(nom: String, dateAchat: LocalDate, montantTTC: Double, dureeAmortissement: Int) {
        viewModelScope.launch {
            assetRepo.insert(
                Asset(
                    nom = nom,
                    dateAchatEpoch = dateAchat.toEpochDay(),
                    montantTTC = montantTTC,
                    dureeAmortissement = dureeAmortissement
                )
            )
        }
    }

    fun deleteAsset(asset: Asset) {
        viewModelScope.launch {
            assetRepo.delete(asset)
        }
    }

    private fun loadAssets() {
        viewModelScope.launch {
            assetRepo.getAll().collectLatest { assets ->
                val today = LocalDate.now()
                val details = assets.map { asset ->
                    val dateAchat = LocalDate.ofEpochDay(asset.dateAchatEpoch)
                    val yearsElapsed = Period.between(dateAchat, today).years.coerceAtLeast(0)
                    val amortAnnuel = asset.montantTTC / asset.dureeAmortissement
                    val amortCumule = (amortAnnuel * yearsElapsed).coerceAtMost(asset.montantTTC)
                    val vnc = (asset.montantTTC - amortCumule).coerceAtLeast(0.0)

                    AssetDetail(
                        asset = asset,
                        amortissementAnnuel = amortAnnuel,
                        amortissementCumule = amortCumule,
                        valeurNetteComptable = vnc
                    )
                }
                _state.value = AssetsState(
                    assets = details,
                    totalValeurNette = details.sumOf { it.valeurNetteComptable },
                    isLoading = false
                )
            }
        }
    }

    class Factory(
        private val assetRepo: AssetRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AssetsViewModel(assetRepo) as T
        }
    }
}
