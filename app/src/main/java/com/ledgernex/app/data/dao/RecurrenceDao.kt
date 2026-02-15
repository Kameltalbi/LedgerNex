package com.ledgernex.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ledgernex.app.data.entity.RecurrenceTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurrenceDao {

    @Insert
    suspend fun insert(template: RecurrenceTemplate): Long

    @Update
    suspend fun update(template: RecurrenceTemplate)

    @Delete
    suspend fun delete(template: RecurrenceTemplate)

    @Query("SELECT * FROM recurrence_templates ORDER BY dateDebutEpoch DESC")
    fun getAll(): Flow<List<RecurrenceTemplate>>

    @Query("SELECT * FROM recurrence_templates WHERE active = 1")
    suspend fun getActiveTemplates(): List<RecurrenceTemplate>

    @Query("SELECT * FROM recurrence_templates WHERE id = :id")
    suspend fun getById(id: Long): RecurrenceTemplate?
}
