package com.ledgernex.app

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.abs

/**
 * Tests unitaires pour la logique d'équilibre du bilan.
 * Vérifie la tolérance Double et les cas limites.
 */
class BilanCalculationTest {

    private fun isBalanced(totalActif: Double, totalPassif: Double): Boolean {
        return abs(totalActif - totalPassif) < 0.01
    }

    @Test
    fun `bilan equilibre exact`() {
        assertThat(isBalanced(10000.0, 10000.0)).isTrue()
    }

    @Test
    fun `bilan equilibre avec tolerance infime`() {
        // Erreur d'arrondi typique des Double
        assertThat(isBalanced(10000.005, 10000.001)).isTrue()
    }

    @Test
    fun `bilan desequilibre clair`() {
        assertThat(isBalanced(10000.0, 9000.0)).isFalse()
    }

    @Test
    fun `bilan desequilibre leger au dessus tolerance`() {
        assertThat(isBalanced(10000.0, 9999.98)).isFalse()
    }

    @Test
    fun `bilan equilibre a zero`() {
        assertThat(isBalanced(0.0, 0.0)).isTrue()
    }

    @Test
    fun `bilan avec valeurs negatives equilibre`() {
        // Résultat négatif peut rendre le passif négatif
        assertThat(isBalanced(-500.0, -500.0)).isTrue()
    }

    @Test
    fun `bilan avec valeurs negatives desequilibre`() {
        assertThat(isBalanced(-500.0, -600.0)).isFalse()
    }

    @Test
    fun `calcul total actif = tresorerie + immobilisations`() {
        val tresorerie = 15000.0
        val valeurNetteImmo = 8000.0
        val totalActif = tresorerie + valeurNetteImmo
        assertThat(totalActif).isEqualTo(23000.0)
    }

    @Test
    fun `calcul total passif = capitaux propres + resultat`() {
        val capitauxPropres = 20000.0
        val resultatExercice = 3000.0
        val totalPassif = capitauxPropres + resultatExercice
        assertThat(totalPassif).isEqualTo(23000.0)
    }
}
