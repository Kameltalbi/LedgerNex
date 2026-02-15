package com.ledgernex.app.data.repository

import com.ledgernex.app.data.dao.TransactionDao
import com.ledgernex.app.data.entity.Transaction
import com.ledgernex.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAll(): Flow<List<Transaction>> = dao.getAll()

    override fun getByDateRange(startEpoch: Long, endEpoch: Long): Flow<List<Transaction>> =
        dao.getByDateRange(startEpoch, endEpoch)

    override fun search(query: String): Flow<List<Transaction>> = dao.search(query)

    override suspend fun getById(id: Long): Transaction? = dao.getById(id)

    override suspend fun insert(transaction: Transaction): Long = dao.insert(transaction)

    override suspend fun insertAll(transactions: List<Transaction>) = dao.insertAll(transactions)

    override suspend fun update(transaction: Transaction) = dao.update(transaction)

    override suspend fun delete(transaction: Transaction) = dao.delete(transaction)

    override suspend fun getResultForPeriod(startEpoch: Long, endEpoch: Long): Double =
        dao.getResultForPeriod(startEpoch, endEpoch)

    override suspend fun getTotalProduits(startEpoch: Long, endEpoch: Long): Double =
        dao.getTotalProduits(startEpoch, endEpoch)

    override suspend fun getTotalCharges(startEpoch: Long, endEpoch: Long): Double =
        dao.getTotalCharges(startEpoch, endEpoch)

    override suspend fun getByRecurrenceId(recurrenceId: Long): List<Transaction> =
        dao.getByRecurrenceId(recurrenceId)

    override suspend fun getFutureUnmodifiedByRecurrence(recurrenceId: Long, fromEpoch: Long): List<Transaction> =
        dao.getFutureUnmodifiedByRecurrence(recurrenceId, fromEpoch)

    override suspend fun deleteUnmodifiedByRecurrence(recurrenceId: Long) =
        dao.deleteUnmodifiedByRecurrence(recurrenceId)

    override suspend fun deleteAllByRecurrence(recurrenceId: Long) =
        dao.deleteAllByRecurrence(recurrenceId)

    override suspend fun deleteFutureUnmodifiedByRecurrence(recurrenceId: Long, fromEpoch: Long) =
        dao.deleteFutureUnmodifiedByRecurrence(recurrenceId, fromEpoch)

    override suspend fun countByRecurrenceAndDate(recurrenceId: Long, dateEpoch: Long): Int =
        dao.countByRecurrenceAndDate(recurrenceId, dateEpoch)
}
