package com.ledgernex.app.domain.repository

import com.ledgernex.app.data.entity.RecurrenceTemplate
import kotlinx.coroutines.flow.Flow

interface RecurrenceRepository {
    fun getAll(): Flow<List<RecurrenceTemplate>>
    suspend fun getActiveTemplates(): List<RecurrenceTemplate>
    suspend fun getById(id: Long): RecurrenceTemplate?
    suspend fun insert(template: RecurrenceTemplate): Long
    suspend fun update(template: RecurrenceTemplate)
    suspend fun delete(template: RecurrenceTemplate)
}
