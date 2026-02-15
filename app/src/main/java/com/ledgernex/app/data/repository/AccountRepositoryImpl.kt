package com.ledgernex.app.data.repository

import com.ledgernex.app.data.dao.AccountDao
import com.ledgernex.app.data.entity.CompanyAccount
import com.ledgernex.app.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class AccountRepositoryImpl(
    private val dao: AccountDao
) : AccountRepository {

    override fun getAll(): Flow<List<CompanyAccount>> = dao.getAll()

    override fun getActiveAccounts(): Flow<List<CompanyAccount>> = dao.getActiveAccounts()

    override suspend fun getById(id: Long): CompanyAccount? = dao.getById(id)

    override suspend fun insert(account: CompanyAccount): Long {
        val activeCount = dao.countActive()
        if (account.actif && activeCount >= 5) {
            throw IllegalStateException("Maximum 5 comptes actifs autorisés")
        }
        return dao.insert(account)
    }

    override suspend fun update(account: CompanyAccount) {
        if (account.actif) {
            val current = dao.getById(account.id)
            if (current != null && !current.actif) {
                val activeCount = dao.countActive()
                if (activeCount >= 5) {
                    throw IllegalStateException("Maximum 5 comptes actifs autorisés")
                }
            }
        }
        dao.update(account)
    }

    override suspend fun delete(account: CompanyAccount) {
        val txCount = dao.countTransactionsForAccount(account.id)
        if (txCount > 0) {
            throw IllegalStateException("Impossible de supprimer un compte avec des transactions liées ($txCount)")
        }
        dao.delete(account)
    }

    override suspend fun countActive(): Int = dao.countActive()

    override suspend fun getAccountBalance(accountId: Long): Double =
        dao.getAccountBalance(accountId)

    override suspend fun countTransactionsForAccount(accountId: Long): Int =
        dao.countTransactionsForAccount(accountId)
}
