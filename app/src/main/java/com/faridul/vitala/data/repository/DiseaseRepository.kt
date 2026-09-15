package com.faridul.vitala.data.repository

import android.content.Context
import com.faridul.vitala.data.local.DiseaseAssetLoader
import com.faridul.vitala.data.local.DiseaseDao
import com.faridul.vitala.data.model.Disease
import com.faridul.vitala.util.LocaleUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DiseaseRepository(private val dao: DiseaseDao) {
    fun observeAll(): Flow<List<Disease>> = dao.observeAll()

    fun observePreview(limit: Int): Flow<List<Disease>> =
        dao.observeAll().map { it.take(limit) }

    fun observeById(id: String): Flow<Disease?> = dao.observeById(id)

    /**
     * Reloads the disease library from the JSON asset matching the app's current
     * language, replacing whatever was seeded before. Cheap (small local JSON,
     * no network), so it's safe to call on every app start rather than tracking
     * whether the language actually changed.
     */
    suspend fun syncLanguage(context: Context) = withContext(Dispatchers.IO) {
        val languageTag = LocaleUtils.currentLanguageTag(context)
        dao.replaceAll(DiseaseAssetLoader.loadForLanguage(context, languageTag))
    }
}
