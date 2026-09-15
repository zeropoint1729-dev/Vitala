package com.faridul.vitala.data.repository

import com.faridul.vitala.data.local.DailyTipDao
import com.faridul.vitala.data.model.DailyTip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

private class FakeDailyTipDao(private val tips: List<DailyTip>) : DailyTipDao {
    override fun observeAll(): Flow<List<DailyTip>> = flowOf(tips)
    override suspend fun insertAll(tips: List<DailyTip>) {
        // no-op: seeding isn't under test here
    }
}

class TipRepositoryTest {

    @Test
    fun `today's tip is picked deterministically by day of year`() = runBlocking {
        val tips = listOf(
            DailyTip("t1", "one", "a"),
            DailyTip("t2", "two", "b"),
            DailyTip("t3", "three", "c")
        )
        val repository = TipRepository(FakeDailyTipDao(tips))

        val expectedIndex = LocalDate.now().dayOfYear % tips.size
        val today = repository.observeTodayTip().first()

        assertEquals(tips[expectedIndex], today)
    }

    @Test
    fun `empty tip list yields null instead of crashing`() = runBlocking {
        val repository = TipRepository(FakeDailyTipDao(emptyList()))
        assertNull(repository.observeTodayTip().first())
    }

    @Test
    fun `single tip is always today's tip`() = runBlocking {
        val onlyTip = DailyTip("t1", "solo", "a")
        val repository = TipRepository(FakeDailyTipDao(listOf(onlyTip)))
        assertEquals(onlyTip, repository.observeTodayTip().first())
    }
}
