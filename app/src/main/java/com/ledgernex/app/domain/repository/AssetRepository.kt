package com.ledgernex.app.domain.repository

import com.ledgernex.app.data.entity.Asset
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    fun getAll(): Flow<List<Asset>>
    suspend fun getById(id: Long): Asset?
    suspend fun insert(asset: Asset): Long
    suspend fun update(asset: Asset)
    suspend fun delete(asset: Asset)
}
