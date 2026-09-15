package com.faridul.vitala.data.repository

import com.faridul.vitala.data.local.NewsArticleDao
import com.faridul.vitala.data.model.NewsArticle
import com.faridul.vitala.data.remote.RemoteNewsSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NewsRepository(
    private val dao: NewsArticleDao,
    private val remoteNewsSource: RemoteNewsSource = RemoteNewsSource()
) {
    fun observeAll(): Flow<List<NewsArticle>> = dao.observeAll()

    fun observePreview(limit: Int): Flow<List<NewsArticle>> =
        dao.observeAll().map { it.take(limit) }

    fun observeById(id: String): Flow<NewsArticle?> = dao.observeById(id)

    /**
     * Fetches live articles from WHO (news) and PubMed (journal) and atomically
     * replaces the cached set. If both sources fail (e.g. no connectivity), the
     * existing cached/seed data is left untouched rather than being cleared.
     */
    suspend fun refresh(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val whoNews = remoteNewsSource.fetchWhoNews(limit = 8)
            val journalArticles = remoteNewsSource.fetchJournalArticles(limit = 6)
            val fresh = whoNews + journalArticles

            if (fresh.isNotEmpty()) {
                dao.replaceAll(fresh)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
