package com.faridul.vitala.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.faridul.vitala.VitalaApplication
import com.faridul.vitala.data.model.DailyTip
import com.faridul.vitala.data.model.Disease
import com.faridul.vitala.data.model.NewsArticle
import com.faridul.vitala.data.repository.DiseaseRepository
import com.faridul.vitala.data.repository.NewsRepository
import com.faridul.vitala.data.repository.TipRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    diseaseRepository: DiseaseRepository,
    newsRepository: NewsRepository,
    tipRepository: TipRepository
) : ViewModel() {

    val todayTip: StateFlow<DailyTip?> = tipRepository.observeTodayTip()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestNews: StateFlow<List<NewsArticle>> = newsRepository.observePreview(3)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diseasePreview: StateFlow<List<Disease>> = diseaseRepository.observePreview(3)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class HomeViewModelFactory(private val app: VitalaApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            app.diseaseRepository,
            app.newsRepository,
            app.tipRepository
        ) as T
    }
}
