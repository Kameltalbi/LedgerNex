package com.ledgernex.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ledgernex.app.data.entity.CompanyAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Insert
    suspend fun insert(account: CompanyAccount): Long

    @Update
    suspend fun update(account: CompanyAccount)

    @Delete
    suspend fun delete(account: CompanyAccount)

    @Query("SELECT * FROM company_accounts ORDER BY nom ASC")
    fun getAll(): Flow<List<CompanyAccount>>

    @Query("SELECT * FROM company_accounts ORDER BY nom ASC")
    suspend fun getAllAccounts(): List<CompanyAccount>

    @Query("SELECT * FROM company_accounts WHERE actif = 1 ORDER BY nom ASC")
    fun getActiveAccounts(): Flow<List<CompanyAccount>>

    @Query("SELECT * FROM company_accounts WHERE id = :id")
    suspend fun getById(id: Long): CompanyAccount?

    @Query("SELECT COUNT(*) FROM company_accounts WHERE actif = 1")
    suspend fun countActive(): Int

    /** Solde dynamique = soldeInitial + recettes - dépenses */
    @Query("""
        SELECT 
            a.soldeInitial 
            + IFNULL(SUM(CASE WHEN t.type = 'RECETTE' THEN t.montantTTC ELSE 0 END), 0)
            - IFNULL(SUM(CASE WHEN t.type = 'DEPENSE' THEN t.montantTTC ELSE 0 END), 0)
        FROM company_accounts a
        LEFT JOIN transactions t ON t.accountId = a.id
        WHERE a.id = :accountId
    """)
    suspend fun getAccountBalance(accountId: Long): Double

    /** Vérifie si des transactions sont liées à ce compte */
    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun countTransactionsForAccount(accountId: Long): Int
}
