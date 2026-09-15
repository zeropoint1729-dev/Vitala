package com.faridul.vitala.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_tips")
data class DailyTip(
    @PrimaryKey val id: String,
    val text: String,
    val category: String
)
