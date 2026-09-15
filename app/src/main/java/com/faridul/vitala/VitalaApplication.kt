package com.faridul.vitala

import android.app.Application
import com.faridul.vitala.data.local.AppDatabase
import com.faridul.vitala.data.repository.DiseaseRepository
import com.faridul.vitala.data.repository.NewsRepository
import com.faridul.vitala.data.repository.TipRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VitalaApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getInstance(this, applicationScope) }
    val diseaseRepository by lazy { DiseaseRepository(database.diseaseDao()) }
    val newsRepository by lazy { NewsRepository(database.newsArticleDao()) }
    val tipRepository by lazy { TipRepository(database.dailyTipDao()) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { newsRepository.refresh() }
    }
}
