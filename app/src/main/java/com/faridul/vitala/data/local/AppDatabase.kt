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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [Disease::class, NewsArticle::class, DailyTip::class],
    version = 1,
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
                ).addCallback(SeedCallback(scope))
                    .build()
                    .also { INSTANCE = it }
            }
    }

    private class SeedCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val instance = INSTANCE ?: return
            scope.launch {
                instance.diseaseDao().insertAll(seedDiseases)
                instance.newsArticleDao().insertAll(seedNews)
                instance.dailyTipDao().insertAll(seedTips)
            }
        }
    }
}

// Starter content only. Real disease entries should come from a reviewed,
// sourced dataset (see README) rather than being expanded here by hand.
private val seedDiseases = listOf(
    Disease(
        id = "diabetes-type-2",
        name = "Type 2 diabetes",
        category = "endocrine",
        overview = "A chronic condition affecting how the body processes blood sugar, caused by insulin resistance or reduced insulin production.",
        symptoms = listOf(
            "Increased thirst and frequent urination",
            "Fatigue and blurred vision",
            "Unexplained weight loss"
        ),
        treatment = "Typically managed with lifestyle changes, oral medication such as metformin, and in some cases insulin therapy. Regular blood glucose monitoring is essential.",
        sourceCitation = "World Health Organization, Diabetes fact sheet"
    ),
    Disease(
        id = "hypertension",
        name = "Hypertension",
        category = "cardiovascular",
        overview = "Persistently elevated blood pressure in the arteries, often with no early symptoms, that raises the risk of heart disease and stroke.",
        symptoms = listOf(
            "Usually asymptomatic in early stages",
            "Headaches or shortness of breath at very high readings",
            "Nosebleeds in severe cases"
        ),
        treatment = "Managed through reduced sodium intake, regular exercise, weight management, and antihypertensive medication such as ACE inhibitors when prescribed.",
        sourceCitation = "World Health Organization, Hypertension fact sheet"
    )
)

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
