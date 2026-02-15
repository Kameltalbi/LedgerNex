package com.ledgernex.app.domain.repository

import com.ledgernex.app.data.entity.CompanyAccount
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAll(): Flow<List<CompanyAccount>>
    fun getActiveAccounts(): Flow<List<CompanyAccount>>
    suspend fun getById(id: Long): CompanyAccount?
    suspend fun insert(account: CompanyAccount): Long
    suspend fun update(account: CompanyAccount)
    suspend fun delete(account: CompanyAccount)
    suspend fun countActive(): Int
    suspend fun getAccountBalance(accountId: Long): Double
    suspend fun countTransactionsForAccount(accountId: Long): Int
}
