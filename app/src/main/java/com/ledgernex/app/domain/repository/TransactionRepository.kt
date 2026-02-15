package com.ledgernex.app.domain.repository

import com.ledgernex.app.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAll(): Flow<List<Transaction>>
    fun getByDateRange(startEpoch: Long, endEpoch: Long): Flow<List<Transaction>>
    fun search(query: String): Flow<List<Transaction>>
    suspend fun getById(id: Long): Transaction?
    suspend fun insert(transaction: Transaction): Long
    suspend fun insertAll(transactions: List<Transaction>)
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    suspend fun getResultForPeriod(startEpoch: Long, endEpoch: Long): Double
    suspend fun getTotalProduits(startEpoch: Long, endEpoch: Long): Double
    suspend fun getTotalCharges(startEpoch: Long, endEpoch: Long): Double
    suspend fun getByRecurrenceId(recurrenceId: Long): List<Transaction>
    suspend fun getFutureUnmodifiedByRecurrence(recurrenceId: Long, fromEpoch: Long): List<Transaction>
    suspend fun deleteUnmodifiedByRecurrence(recurrenceId: Long)
    suspend fun deleteAllByRecurrence(recurrenceId: Long)
    suspend fun deleteFutureUnmodifiedByRecurrence(recurrenceId: Long, fromEpoch: Long)
    suspend fun countByRecurrenceAndDate(recurrenceId: Long, dateEpoch: Long): Int
}
