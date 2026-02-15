package com.ledgernex.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ledgernex.app.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Insert
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY dateEpoch DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    /** Transactions dans un intervalle de dates (mois, année, etc.) */
    @Query("SELECT * FROM transactions WHERE dateEpoch BETWEEN :startEpoch AND :endEpoch ORDER BY dateEpoch DESC")
    fun getByDateRange(startEpoch: Long, endEpoch: Long): Flow<List<Transaction>>

    /** Recherche par libellé ou objet */
    @Query("""
        SELECT * FROM transactions 
        WHERE libelle LIKE '%' || :query || '%' 
           OR objet LIKE '%' || :query || '%'
        ORDER BY dateEpoch DESC
    """)
    fun search(query: String): Flow<List<Transaction>>

    /** Résultat (recettes - dépenses) sur un intervalle */
    @Query("""
        SELECT 
            IFNULL(SUM(CASE WHEN type = 'RECETTE' THEN montantTTC ELSE 0 END), 0)
            - IFNULL(SUM(CASE WHEN type = 'DEPENSE' THEN montantTTC ELSE 0 END), 0)
        FROM transactions
        WHERE dateEpoch BETWEEN :startEpoch AND :endEpoch
    """)
    suspend fun getResultForPeriod(startEpoch: Long, endEpoch: Long): Double

    /** Total produits sur un intervalle */
    @Query("""
        SELECT IFNULL(SUM(montantTTC), 0)
        FROM transactions
        WHERE type = 'RECETTE' AND dateEpoch BETWEEN :startEpoch AND :endEpoch
    """)
    suspend fun getTotalProduits(startEpoch: Long, endEpoch: Long): Double

    /** Total charges sur un intervalle */
    @Query("""
        SELECT IFNULL(SUM(montantTTC), 0)
        FROM transactions
        WHERE type = 'DEPENSE' AND dateEpoch BETWEEN :startEpoch AND :endEpoch
    """)
    suspend fun getTotalCharges(startEpoch: Long, endEpoch: Long): Double

    /** Transactions liées à une récurrence */
    @Query("SELECT * FROM transactions WHERE recurrenceId = :recurrenceId ORDER BY dateEpoch ASC")
    suspend fun getByRecurrenceId(recurrenceId: Long): List<Transaction>

    /** Transactions liées à une récurrence, non modifiées, futures */
    @Query("""
        SELECT * FROM transactions 
        WHERE recurrenceId = :recurrenceId 
          AND isModified = 0 
          AND dateEpoch >= :fromEpoch
        ORDER BY dateEpoch ASC
    """)
    suspend fun getFutureUnmodifiedByRecurrence(recurrenceId: Long, fromEpoch: Long): List<Transaction>

    /** Supprime toutes les transactions non modifiées d'une récurrence */
    @Query("DELETE FROM transactions WHERE recurrenceId = :recurrenceId AND isModified = 0")
    suspend fun deleteUnmodifiedByRecurrence(recurrenceId: Long)

    /** Supprime toutes les transactions d'une récurrence */
    @Query("DELETE FROM transactions WHERE recurrenceId = :recurrenceId")
    suspend fun deleteAllByRecurrence(recurrenceId: Long)

    /** Supprime les futures non modifiées à partir d'une date */
    @Query("""
        DELETE FROM transactions 
        WHERE recurrenceId = :recurrenceId 
          AND isModified = 0 
          AND dateEpoch >= :fromEpoch
    """)
    suspend fun deleteFutureUnmodifiedByRecurrence(recurrenceId: Long, fromEpoch: Long)

    /** Vérifie si une transaction existe déjà pour (recurrenceId, dateEpoch) */
    @Query("""
        SELECT COUNT(*) FROM transactions 
        WHERE recurrenceId = :recurrenceId AND dateEpoch = :dateEpoch
    """)
    suspend fun countByRecurrenceAndDate(recurrenceId: Long, dateEpoch: Long): Int

    /** Filtrer par date + compte */
    @Query("""
        SELECT * FROM transactions 
        WHERE dateEpoch BETWEEN :startEpoch AND :endEpoch AND accountId = :accountId
        ORDER BY dateEpoch DESC
    """)
    fun getByDateRangeAndAccount(startEpoch: Long, endEpoch: Long, accountId: Long): Flow<List<Transaction>>

    /** Filtrer par date + catégorie */
    @Query("""
        SELECT * FROM transactions 
        WHERE dateEpoch BETWEEN :startEpoch AND :endEpoch AND categorie = :categorie
        ORDER BY dateEpoch DESC
    """)
    fun getByDateRangeAndCategory(startEpoch: Long, endEpoch: Long, categorie: String): Flow<List<Transaction>>

    /** Filtrer par date + compte + catégorie */
    @Query("""
        SELECT * FROM transactions 
        WHERE dateEpoch BETWEEN :startEpoch AND :endEpoch 
          AND accountId = :accountId AND categorie = :categorie
        ORDER BY dateEpoch DESC
    """)
    fun getByDateRangeAccountAndCategory(startEpoch: Long, endEpoch: Long, accountId: Long, categorie: String): Flow<List<Transaction>>

    /** Total recettes pour un compte */
    @Query("""
        SELECT IFNULL(SUM(montantTTC), 0) FROM transactions 
        WHERE type = 'RECETTE' AND accountId = :accountId
    """)
    suspend fun getTotalRecettesForAccount(accountId: Long): Double

    /** Total dépenses pour un compte */
    @Query("""
        SELECT IFNULL(SUM(montantTTC), 0) FROM transactions 
        WHERE type = 'DEPENSE' AND accountId = :accountId
    """)
    suspend fun getTotalDepensesForAccount(accountId: Long): Double

    /** Dernières transactions d'un compte */
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY dateEpoch DESC LIMIT :limit")
    suspend fun getRecentByAccount(accountId: Long, limit: Int = 10): List<Transaction>

    /** Transactions dans un intervalle de dates (retourne List pour export) */
    @Query("SELECT * FROM transactions WHERE dateEpoch BETWEEN :startEpoch AND :endEpoch ORDER BY dateEpoch DESC")
    suspend fun getTransactionsByDateRange(startEpoch: Long, endEpoch: Long): List<Transaction>

    /** Résultat mensuel pour chaque mois d'une année (pour graphique dashboard) */
    @Query("""
        SELECT 
            IFNULL(SUM(CASE WHEN type = 'RECETTE' THEN montantTTC ELSE 0 END), 0)
            - IFNULL(SUM(CASE WHEN type = 'DEPENSE' THEN montantTTC ELSE 0 END), 0)
        FROM transactions
        WHERE dateEpoch BETWEEN :startEpoch AND :endEpoch
    """)
    suspend fun getMonthlyResult(startEpoch: Long, endEpoch: Long): Double
}
