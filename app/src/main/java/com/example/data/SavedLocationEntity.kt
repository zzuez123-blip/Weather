package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val temp: Int,
    val weatherStateId: String,
    val isCustom: Boolean = false
)
