package com.ledgernex.app.data.repository

import com.ledgernex.app.data.dao.RecurrenceDao
import com.ledgernex.app.data.entity.RecurrenceTemplate
import com.ledgernex.app.domain.repository.RecurrenceRepository
import kotlinx.coroutines.flow.Flow

class RecurrenceRepositoryImpl(
    private val dao: RecurrenceDao
) : RecurrenceRepository {

    override fun getAll(): Flow<List<RecurrenceTemplate>> = dao.getAll()

    override suspend fun getActiveTemplates(): List<RecurrenceTemplate> = dao.getActiveTemplates()

    override suspend fun getById(id: Long): RecurrenceTemplate? = dao.getById(id)

    override suspend fun insert(template: RecurrenceTemplate): Long = dao.insert(template)

    override suspend fun update(template: RecurrenceTemplate) = dao.update(template)

    override suspend fun delete(template: RecurrenceTemplate) = dao.delete(template)
}
