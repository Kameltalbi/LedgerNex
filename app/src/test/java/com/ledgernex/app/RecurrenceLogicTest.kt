package com.ledgernex.app

import com.google.common.truth.Truth.assertThat
import com.ledgernex.app.data.entity.RecurrenceFrequency
import com.ledgernex.app.data.entity.RecurrenceTemplate
import org.junit.Test
import java.time.LocalDate

/**
 * Tests unitaires pour la logique de récurrence (génération de dates).
 * Teste la logique pure sans dépendances Room.
 */
class RecurrenceLogicTest {

    /**
     * Simule la génération des dates mensuelles pour un template.
     * Retourne la liste des epochDay à générer.
     */
    private fun generateDates(
        template: RecurrenceTemplate,
        today: LocalDate,
        limit: LocalDate
    ): List<Long> {
        val start = LocalDate.ofEpochDay(template.dateDebutEpoch)
        val dateFin = template.dateFinEpoch
        val end = if (dateFin != null) {
            val templateEnd = LocalDate.ofEpochDay(dateFin)
            if (templateEnd.isBefore(limit)) templateEnd else limit
        } else {
            limit
        }

        val dates = mutableListOf<Long>()
        var current = start
        while (!current.isAfter(end)) {
            dates.add(current.toEpochDay())
            current = current.plusMonths(1)
        }
        return dates
    }

    private fun createTemplate(
        dateDebut: LocalDate,
        dateFin: LocalDate? = null
    ): RecurrenceTemplate {
        return RecurrenceTemplate(
            id = 1,
            libelle = "Loyer",
            objet = "Bureau",
            montantTTC = -1500.0,
            categorie = "Loyer",
            accountId = 1,
            frequence = RecurrenceFrequency.MONTHLY,
            dateDebutEpoch = dateDebut.toEpochDay(),
            dateFinEpoch = dateFin?.toEpochDay(),
            active = true
        )
    }

    @Test
    fun `genere 2 dates pour template debutant il y a 1 mois`() {
        val today = LocalDate.of(2025, 3, 15)
        val limit = today.plusMonths(1) // 2025-04-15
        val template = createTemplate(dateDebut = LocalDate.of(2025, 2, 15))

        val dates = generateDates(template, today, limit)

        // 2025-02-15, 2025-03-15, 2025-04-15
        assertThat(dates).hasSize(3)
    }

    @Test
    fun `ne genere pas au dela de la limite 1 mois`() {
        val today = LocalDate.of(2025, 1, 1)
        val limit = today.plusMonths(1) // 2025-02-01
        val template = createTemplate(dateDebut = LocalDate.of(2025, 1, 1))

        val dates = generateDates(template, today, limit)

        // 2025-01-01, 2025-02-01
        assertThat(dates).hasSize(2)
        assertThat(LocalDate.ofEpochDay(dates.last())).isEqualTo(LocalDate.of(2025, 2, 1))
    }

    @Test
    fun `respecte dateFin du template`() {
        val today = LocalDate.of(2025, 6, 1)
        val limit = today.plusMonths(1)
        val template = createTemplate(
            dateDebut = LocalDate.of(2025, 1, 1),
            dateFin = LocalDate.of(2025, 3, 1)
        )

        val dates = generateDates(template, today, limit)

        // 2025-01-01, 2025-02-01, 2025-03-01 (dateFin = 2025-03-01 < limit)
        assertThat(dates).hasSize(3)
        assertThat(LocalDate.ofEpochDay(dates.last())).isEqualTo(LocalDate.of(2025, 3, 1))
    }

    @Test
    fun `template futur ne genere rien avant dateDebut`() {
        val today = LocalDate.of(2025, 1, 1)
        val limit = today.plusMonths(1) // 2025-02-01
        val template = createTemplate(dateDebut = LocalDate.of(2025, 6, 1))

        val dates = generateDates(template, today, limit)

        // dateDebut (2025-06-01) est après limit (2025-02-01)
        assertThat(dates).isEmpty()
    }

    @Test
    fun `template avec dateDebut egale a limit genere 1 date`() {
        val today = LocalDate.of(2025, 1, 1)
        val limit = today.plusMonths(1) // 2025-02-01
        val template = createTemplate(dateDebut = LocalDate.of(2025, 2, 1))

        val dates = generateDates(template, today, limit)

        assertThat(dates).hasSize(1)
        assertThat(LocalDate.ofEpochDay(dates[0])).isEqualTo(LocalDate.of(2025, 2, 1))
    }

    @Test
    fun `generation mensuelle produit des dates espacees de 1 mois`() {
        val today = LocalDate.of(2025, 6, 1)
        val limit = today.plusMonths(1)
        val template = createTemplate(dateDebut = LocalDate.of(2025, 1, 1))

        val dates = generateDates(template, today, limit)

        // Vérifier que chaque date est espacée de 1 mois
        for (i in 1 until dates.size) {
            val prev = LocalDate.ofEpochDay(dates[i - 1])
            val curr = LocalDate.ofEpochDay(dates[i])
            assertThat(curr).isEqualTo(prev.plusMonths(1))
        }
    }
}
