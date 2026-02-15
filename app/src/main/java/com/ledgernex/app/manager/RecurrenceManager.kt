package com.ledgernex.app.manager

import com.ledgernex.app.data.entity.RecurrenceTemplate
import com.ledgernex.app.data.entity.Transaction
import com.ledgernex.app.data.entity.TransactionType
import com.ledgernex.app.domain.repository.RecurrenceRepository
import com.ledgernex.app.domain.repository.TransactionRepository
import java.time.LocalDate

/**
 * Gère la génération intelligente des transactions récurrentes.
 *
 * Règles :
 * - Génère au maximum 1 mois à l'avance
 * - Idempotent : ne crée jamais de doublon pour (recurrenceId, dateEpoch)
 * - Respecte isModified (ne touche pas aux occurrences modifiées manuellement)
 */
class RecurrenceManager(
    private val recurrenceRepo: RecurrenceRepository,
    private val transactionRepo: TransactionRepository
) {

    /**
     * Génère les transactions récurrentes manquantes pour tous les templates actifs.
     * Appelé au lancement de l'app ou à l'ouverture de l'écran Transactions.
     */
    suspend fun generatePendingTransactions() {
        val today = LocalDate.now()
        val limit = today.plusMonths(1) // max 1 mois à l'avance

        val templates = recurrenceRepo.getActiveTemplates()
        for (template in templates) {
            generateForTemplate(template, today, limit)
        }
    }

    /**
     * Génère les transactions manquantes pour un template donné,
     * de sa dateDebut jusqu'à [limit] (ou dateFin si antérieure).
     */
    suspend fun generateForTemplate(
        template: RecurrenceTemplate,
        today: LocalDate = LocalDate.now(),
        limit: LocalDate = today.plusMonths(1)
    ) {
        val start = LocalDate.ofEpochDay(template.dateDebutEpoch)
        val end = if (template.dateFinEpoch != null) {
            val templateEnd = LocalDate.ofEpochDay(template.dateFinEpoch)
            if (templateEnd.isBefore(limit)) templateEnd else limit
        } else {
            limit
        }

        var current = start
        while (!current.isAfter(end)) {
            val epoch = current.toEpochDay()

            // Idempotence : vérifier qu'aucune transaction n'existe déjà pour cette date
            val exists = transactionRepo.countByRecurrenceAndDate(template.id, epoch)
            if (exists == 0) {
                val transaction = Transaction(
                    type = if (template.montantTTC >= 0) TransactionType.RECETTE else TransactionType.DEPENSE,
                    dateEpoch = epoch,
                    libelle = template.libelle,
                    objet = template.objet,
                    montantTTC = kotlin.math.abs(template.montantTTC),
                    categorie = template.categorie,
                    accountId = template.accountId,
                    recurrenceId = template.id,
                    isModified = false
                )
                transactionRepo.insert(transaction)
            }

            current = current.plusMonths(1)
        }
    }

    // ── Modification d'occurrence ──────────────────────────────────────

    /**
     * Modifier cette occurrence uniquement.
     * Marque la transaction comme isModified = true.
     */
    suspend fun modifyOccurrence(transaction: Transaction) {
        transactionRepo.update(transaction.copy(isModified = true))
    }

    /**
     * Modifier toutes les occurrences futures (non modifiées).
     * Met à jour le template + supprime les futures non modifiées + regénère.
     */
    suspend fun modifyFutureOccurrences(
        template: RecurrenceTemplate,
        fromDate: LocalDate
    ) {
        recurrenceRepo.update(template)
        transactionRepo.deleteFutureUnmodifiedByRecurrence(
            template.id,
            fromDate.toEpochDay()
        )
        generateForTemplate(template)
    }

    /**
     * Modifier toute la série.
     * Met à jour le template + supprime toutes les non modifiées + regénère.
     */
    suspend fun modifySeries(template: RecurrenceTemplate) {
        recurrenceRepo.update(template)
        transactionRepo.deleteUnmodifiedByRecurrence(template.id)
        generateForTemplate(template)
    }

    // ── Suppression ────────────────────────────────────────────────────

    /**
     * Supprimer une seule occurrence.
     */
    suspend fun deleteOccurrence(transaction: Transaction) {
        transactionRepo.delete(transaction)
    }

    /**
     * Supprimer toute la série (template + toutes les transactions liées).
     */
    suspend fun deleteSeries(template: RecurrenceTemplate) {
        transactionRepo.deleteAllByRecurrence(template.id)
        recurrenceRepo.delete(template)
    }
}
