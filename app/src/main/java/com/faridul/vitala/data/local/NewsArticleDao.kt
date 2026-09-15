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

    @Query("SELECT * FROM news_articles WHERE id = :id")
    fun observeById(id: String): Flow<NewsArticle?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<NewsArticle>)

    @Query("DELETE FROM news_articles")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(articles: List<NewsArticle>) {
        clearAll()
        insertAll(articles)
    }
}
