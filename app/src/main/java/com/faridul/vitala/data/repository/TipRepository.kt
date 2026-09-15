package com.faridul.vitala.data.repository

import com.faridul.vitala.data.local.DailyTipDao
import com.faridul.vitala.data.model.DailyTip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class TipRepository(private val dao: DailyTipDao) {
    fun observeAll(): Flow<List<DailyTip>> = dao.observeAll()

    fun observeTodayTip(): Flow<DailyTip?> = dao.observeAll().map { tips ->
        if (tips.isEmpty()) null else tips[LocalDate.now().dayOfYear % tips.size]
    }
}
