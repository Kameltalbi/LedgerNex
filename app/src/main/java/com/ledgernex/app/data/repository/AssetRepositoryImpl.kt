package com.ledgernex.app.data.repository

import com.ledgernex.app.data.dao.AssetDao
import com.ledgernex.app.data.entity.Asset
import com.ledgernex.app.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow

class AssetRepositoryImpl(
    private val dao: AssetDao
) : AssetRepository {

    override fun getAll(): Flow<List<Asset>> = dao.getAll()

    override suspend fun getById(id: Long): Asset? = dao.getById(id)

    override suspend fun insert(asset: Asset): Long = dao.insert(asset)

    override suspend fun update(asset: Asset) = dao.update(asset)

    override suspend fun delete(asset: Asset) = dao.delete(asset)
}
