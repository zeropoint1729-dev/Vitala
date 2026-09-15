package com.faridul.vitala.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.faridul.vitala.data.model.Disease
import kotlinx.coroutines.flow.Flow

@Dao
interface DiseaseDao {
    @Query("SELECT * FROM diseases ORDER BY name ASC")
    fun observeAll(): Flow<List<Disease>>

    @Query("SELECT * FROM diseases WHERE id = :id")
    fun observeById(id: String): Flow<Disease?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(diseases: List<Disease>)
}
