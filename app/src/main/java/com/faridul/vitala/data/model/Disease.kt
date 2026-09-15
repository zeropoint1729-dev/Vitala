package com.faridul.vitala.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diseases")
data class Disease(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val overview: String,
    val symptoms: List<String>,
    val treatment: String,
    val sourceCitation: String
)
