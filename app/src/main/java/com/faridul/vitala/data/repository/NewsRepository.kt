package com.faridul.vitala.data.repository

import com.faridul.vitala.data.local.NewsArticleDao
import com.faridul.vitala.data.model.NewsArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewsRepository(private val dao: NewsArticleDao) {
    fun observeAll(): Flow<List<NewsArticle>> = dao.observeAll()

    fun observePreview(limit: Int): Flow<List<NewsArticle>> =
        dao.observeAll().map { it.take(limit) }

    fun observeById(id: String): Flow<NewsArticle?> = dao.observeById(id)
}
