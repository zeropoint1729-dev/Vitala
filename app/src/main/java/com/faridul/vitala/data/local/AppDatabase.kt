package com.faridul.vitala.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.faridul.vitala.data.model.DailyTip
import com.faridul.vitala.data.model.Disease
import com.faridul.vitala.data.model.NewsArticle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [Disease::class, NewsArticle::class, DailyTip::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diseaseDao(): DiseaseDao
    abstract fun newsArticleDao(): NewsArticleDao
    abstract fun dailyTipDao(): DailyTipDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vitala.db"
                ).addCallback(SeedCallback(context.applicationContext, scope))
                    // Pre-release app, no shipped user data to preserve yet — revisit
                    // with a real Migration before this ships to real users.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }

    private class SeedCallback(
        private val context: Context,
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val instance = INSTANCE ?: return
            scope.launch {
                instance.diseaseDao().insertAll(loadDiseasesFromAssets(context))
                instance.newsArticleDao().insertAll(seedNews)
                instance.dailyTipDao().insertAll(seedTips)
            }
        }
    }
}

// The disease library reads from app/src/main/assets/diseases.json — edit or
// extend that file to grow the dataset; nothing about a disease is hardcoded
// here anymore.
private fun loadDiseasesFromAssets(context: Context): List<Disease> {
    val json = context.assets.open("diseases.json").bufferedReader().use { it.readText() }
    val listType = object : TypeToken<List<Disease>>() {}.type
    return Gson().fromJson(json, listType)
}

private val seedNews = listOf(
    NewsArticle(
        id = "who-flu-guidance",
        title = "WHO updates guidance on seasonal flu vaccination",
        category = "news",
        source = "Reuters Health",
        publishedAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000,
        summary = "New recommendations on timing and priority groups for the coming flu season.",
        url = "https://www.who.int/"
    ),
    NewsArticle(
        id = "lancet-sleep-heart",
        title = "New study links sleep quality to heart health",
        category = "journal",
        source = "The Lancet",
        publishedAt = System.currentTimeMillis() - 24 * 60 * 60 * 1000,
        summary = "Researchers found a consistent association between poor sleep and cardiovascular risk markers.",
        url = "https://www.thelancet.com/"
    )
)

private val seedTips = listOf(
    DailyTip(id = "tip-001", text = "Add 10 minutes of morning stretching to improve circulation.", category = "movement"),
    DailyTip(id = "tip-002", text = "Aim for at least 7 glasses of water spread through the day.", category = "hydration"),
    DailyTip(id = "tip-003", text = "Take a 5-minute screen break every hour to rest your eyes.", category = "eye health"),
    DailyTip(id = "tip-004", text = "Add one extra serving of vegetables to today's largest meal.", category = "nutrition"),
    DailyTip(id = "tip-005", text = "Aim for a consistent bedtime tonight, even on weekends.", category = "sleep")
)
