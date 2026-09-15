package com.faridul.vitala.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.faridul.vitala.data.model.DailyTip
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTipDao {
    @Query("SELECT * FROM daily_tips ORDER BY id ASC")
    fun observeAll(): Flow<List<DailyTip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tips: List<DailyTip>)
}
