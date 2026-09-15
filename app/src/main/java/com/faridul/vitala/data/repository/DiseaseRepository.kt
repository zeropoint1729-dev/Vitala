package com.faridul.vitala.data.repository

import com.faridul.vitala.data.local.DiseaseDao
import com.faridul.vitala.data.model.Disease
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiseaseRepository(private val dao: DiseaseDao) {
    fun observeAll(): Flow<List<Disease>> = dao.observeAll()

    fun observePreview(limit: Int): Flow<List<Disease>> =
        dao.observeAll().map { it.take(limit) }

    fun observeById(id: String): Flow<Disease?> = dao.observeById(id)
}
