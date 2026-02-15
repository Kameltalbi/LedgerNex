package com.ledgernex.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ledgernex.app.data.entity.Asset
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Insert
    suspend fun insert(asset: Asset): Long

    @Update
    suspend fun update(asset: Asset)

    @Delete
    suspend fun delete(asset: Asset)

    @Query("SELECT * FROM assets ORDER BY dateAchatEpoch DESC")
    fun getAll(): Flow<List<Asset>>

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getById(id: Long): Asset?
}
