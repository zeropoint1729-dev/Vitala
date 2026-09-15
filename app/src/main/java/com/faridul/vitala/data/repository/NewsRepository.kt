package com.faridul.vitala.data.repository

import com.faridul.vitala.data.local.NewsArticleDao
import com.faridul.vitala.data.model.NewsArticle
import com.faridul.vitala.data.remote.RemoteNewsSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NewsRepository(
    private val dao: NewsArticleDao,
    private val remoteNewsSource: RemoteNewsSource = RemoteNewsSource()
) {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastSyncedAt = MutableStateFlow<Long?>(null)
    val lastSyncedAt: StateFlow<Long?> = _lastSyncedAt.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun observeAll(): Flow<List<NewsArticle>> = dao.observeAll()

    /**
     * For the home preview specifically: general news first, journal articles
     * only filling remaining slots. observeAll() (the News tab) stays pure
     * recency order — this only changes what surfaces on Home, since a PubMed
     * academic title is a worse first impression than a WHO news item.
     */
    fun observePreview(limit: Int): Flow<List<NewsArticle>> =
        dao.observeAll().map { articles ->
            val news = articles.filter { it.category != "journal" }
            val journals = articles.filter { it.category == "journal" }
            (news + journals).take(limit)
        }

    fun observeById(id: String): Flow<NewsArticle?> = dao.observeById(id)

    fun observeBookmarked(): Flow<List<NewsArticle>> = dao.observeBookmarked()

    suspend fun toggleBookmark(id: String, bookmarked: Boolean) {
        dao.setBookmarked(id, bookmarked)
    }

    /**
     * Fetches live articles from WHO (news) and PubMed (journal) and atomically
     * replaces the non-bookmarked cache. If both sources fail (e.g. no connectivity),
     * the existing cached/seed data is left untouched rather than being cleared, and
     * lastError is set so the UI can surface it.
     */
    suspend fun refresh(): Result<Unit> = withContext(Dispatchers.IO) {
        _isRefreshing.value = true
        try {
            val whoNews = remoteNewsSource.fetchWhoNews(limit = 8)
            val journalArticles = remoteNewsSource.fetchJournalArticles(limit = 6)
            val fresh = whoNews + journalArticles

            if (fresh.isNotEmpty()) {
                dao.replaceAll(fresh)
                _lastSyncedAt.value = System.currentTimeMillis()
                _lastError.value = null
            } else {
                _lastError.value = "empty"
            }
            Result.success(Unit)
        } catch (e: Exception) {
            _lastError.value = e.message ?: "error"
            Result.failure(e)
        } finally {
            _isRefreshing.value = false
        }
    }
}
