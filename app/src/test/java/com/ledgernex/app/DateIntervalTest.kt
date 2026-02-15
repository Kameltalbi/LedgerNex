package com.ledgernex.app

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * Tests unitaires pour la logique d'intervalles de dates (epochDay).
 * Vérifie que les bornes mensuelles et annuelles sont correctes.
 */
class DateIntervalTest {

    @Test
    fun `intervalle mensuel janvier 2025`() {
        val year = 2025
        val month = 1
        val startEpoch = LocalDate.of(year, month, 1).toEpochDay()
        val endEpoch = YearMonth.of(year, month).atEndOfMonth().toEpochDay()

        assertThat(LocalDate.ofEpochDay(startEpoch)).isEqualTo(LocalDate.of(2025, 1, 1))
        assertThat(LocalDate.ofEpochDay(endEpoch)).isEqualTo(LocalDate.of(2025, 1, 31))
    }

    @Test
    fun `intervalle mensuel fevrier 2024 annee bissextile`() {
        val year = 2024
        val month = 2
        val startEpoch = LocalDate.of(year, month, 1).toEpochDay()
        val endEpoch = YearMonth.of(year, month).atEndOfMonth().toEpochDay()

        assertThat(LocalDate.ofEpochDay(startEpoch)).isEqualTo(LocalDate.of(2024, 2, 1))
        assertThat(LocalDate.ofEpochDay(endEpoch)).isEqualTo(LocalDate.of(2024, 2, 29))
    }

    @Test
    fun `intervalle mensuel fevrier 2025 annee non bissextile`() {
        val year = 2025
        val month = 2
        val endEpoch = YearMonth.of(year, month).atEndOfMonth().toEpochDay()

        assertThat(LocalDate.ofEpochDay(endEpoch)).isEqualTo(LocalDate.of(2025, 2, 28))
    }

    @Test
    fun `intervalle annuel 2025`() {
        val year = 2025
        val startEpoch = LocalDate.of(year, 1, 1).toEpochDay()
        val endEpoch = LocalDate.of(year, 12, 31).toEpochDay()

        assertThat(LocalDate.ofEpochDay(startEpoch)).isEqualTo(LocalDate.of(2025, 1, 1))
        assertThat(LocalDate.ofEpochDay(endEpoch)).isEqualTo(LocalDate.of(2025, 12, 31))
    }

    @Test
    fun `date dans intervalle mensuel`() {
        val year = 2025
        val month = 3
        val startEpoch = LocalDate.of(year, month, 1).toEpochDay()
        val endEpoch = YearMonth.of(year, month).atEndOfMonth().toEpochDay()

        val dateInRange = LocalDate.of(2025, 3, 15).toEpochDay()
        val dateBeforeRange = LocalDate.of(2025, 2, 28).toEpochDay()
        val dateAfterRange = LocalDate.of(2025, 4, 1).toEpochDay()

        assertThat(dateInRange in startEpoch..endEpoch).isTrue()
        assertThat(dateBeforeRange in startEpoch..endEpoch).isFalse()
        assertThat(dateAfterRange in startEpoch..endEpoch).isFalse()
    }

    @Test
    fun `epochDay conversion aller-retour`() {
        val original = LocalDate.of(2025, 6, 15)
        val epoch = original.toEpochDay()
        val restored = LocalDate.ofEpochDay(epoch)
        assertThat(restored).isEqualTo(original)
    }
}
