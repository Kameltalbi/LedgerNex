package com.ledgernex.app

import com.google.common.truth.Truth.assertThat
import com.ledgernex.app.data.entity.Asset
import org.junit.Test
import java.time.LocalDate
import java.time.Period

/**
 * Tests unitaires pour le calcul d'amortissement des immobilisations.
 */
class AssetAmortizationTest {

    private fun calculateNetAssetValue(asset: Asset, today: LocalDate = LocalDate.now()): Double {
        val yearsElapsed = Period.between(
            LocalDate.ofEpochDay(asset.dateAchatEpoch),
            today
        ).years
        val amortissement = (asset.montantTTC / asset.dureeAmortissement) * yearsElapsed
        return (asset.montantTTC - amortissement).coerceAtLeast(0.0)
    }

    @Test
    fun `immobilisation neuve valeur nette egale montant`() {
        val today = LocalDate.of(2025, 6, 15)
        val asset = Asset(
            id = 1,
            nom = "Ordinateur",
            dateAchatEpoch = today.toEpochDay(),
            montantTTC = 1200.0,
            dureeAmortissement = 3
        )
        val vnc = calculateNetAssetValue(asset, today)
        assertThat(vnc).isEqualTo(1200.0)
    }

    @Test
    fun `immobilisation apres 1 an sur 3`() {
        val dateAchat = LocalDate.of(2024, 1, 1)
        val today = LocalDate.of(2025, 6, 15) // 1 an révolu
        val asset = Asset(
            id = 1,
            nom = "Mobilier",
            dateAchatEpoch = dateAchat.toEpochDay(),
            montantTTC = 3000.0,
            dureeAmortissement = 3
        )
        val vnc = calculateNetAssetValue(asset, today)
        // 3000 - (3000/3 * 1) = 3000 - 1000 = 2000
        assertThat(vnc).isEqualTo(2000.0)
    }

    @Test
    fun `immobilisation totalement amortie`() {
        val dateAchat = LocalDate.of(2020, 1, 1)
        val today = LocalDate.of(2025, 6, 15) // 5 ans révolus
        val asset = Asset(
            id = 1,
            nom = "Ancien serveur",
            dateAchatEpoch = dateAchat.toEpochDay(),
            montantTTC = 5000.0,
            dureeAmortissement = 3
        )
        val vnc = calculateNetAssetValue(asset, today)
        // 5000 - (5000/3 * 5) = 5000 - 8333 => coerceAtLeast(0.0)
        assertThat(vnc).isEqualTo(0.0)
    }

    @Test
    fun `immobilisation a mi-duree`() {
        val dateAchat = LocalDate.of(2023, 1, 1)
        val today = LocalDate.of(2025, 6, 15) // 2 ans révolus
        val asset = Asset(
            id = 1,
            nom = "Véhicule",
            dateAchatEpoch = dateAchat.toEpochDay(),
            montantTTC = 20000.0,
            dureeAmortissement = 5
        )
        val vnc = calculateNetAssetValue(asset, today)
        // 20000 - (20000/5 * 2) = 20000 - 8000 = 12000
        assertThat(vnc).isEqualTo(12000.0)
    }

    @Test
    fun `amortissement annuel correct`() {
        val asset = Asset(
            id = 1,
            nom = "Machine",
            dateAchatEpoch = LocalDate.of(2025, 1, 1).toEpochDay(),
            montantTTC = 10000.0,
            dureeAmortissement = 5
        )
        val amortissementAnnuel = asset.montantTTC / asset.dureeAmortissement
        assertThat(amortissementAnnuel).isEqualTo(2000.0)
    }

    @Test
    fun `amortissement mensuel correct`() {
        val asset = Asset(
            id = 1,
            nom = "Machine",
            dateAchatEpoch = LocalDate.of(2025, 1, 1).toEpochDay(),
            montantTTC = 12000.0,
            dureeAmortissement = 5
        )
        val amortissementMensuel = asset.montantTTC / asset.dureeAmortissement / 12
        assertThat(amortissementMensuel).isEqualTo(200.0)
    }
}
