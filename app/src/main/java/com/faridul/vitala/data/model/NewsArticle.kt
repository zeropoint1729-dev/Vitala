package com.faridul.vitala.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_articles")
data class NewsArticle(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // "news" or "journal"
    val source: String,
    val publishedAt: Long,
    val summary: String,
    val url: String,
    val isBookmarked: Boolean = false
)
