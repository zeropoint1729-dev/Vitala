package com.faridul.vitala.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.faridul.vitala.data.model.NewsArticle
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsArticleDao {
    @Query("SELECT * FROM news_articles ORDER BY publishedAt DESC")
    fun observeAll(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news_articles WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun observeBookmarked(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM news_articles WHERE id = :id")
    fun observeById(id: String): Flow<NewsArticle?>

    @Query("SELECT id FROM news_articles WHERE isBookmarked = 1")
    suspend fun getBookmarkedIds(): List<String>

    @Query("UPDATE news_articles SET isBookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmarked(id: String, bookmarked: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<NewsArticle>)

    @Query("DELETE FROM news_articles WHERE isBookmarked = 0")
    suspend fun clearUnbookmarked()

    /**
     * Replaces the non-bookmarked cache with fresh articles. Bookmarked rows are
     * never deleted here (so saved articles survive a refresh even if the remote
     * source stops returning them), and if a fresh article shares an id with an
     * existing bookmarked row, its bookmark flag is carried over onto the new copy.
     */
    @Transaction
    suspend fun replaceAll(articles: List<NewsArticle>) {
        val bookmarkedIds = getBookmarkedIds().toSet()
        val merged = articles.map { article ->
            if (article.id in bookmarkedIds) article.copy(isBookmarked = true) else article
        }
        clearUnbookmarked()
        insertAll(merged)
    }
}
