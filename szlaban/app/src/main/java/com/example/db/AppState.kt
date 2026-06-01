package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_state")
data class AppState(
    @PrimaryKey val id: Int = 1,
    val hash: String? = null,
    val errorCount: Int = 0,
    val lockoutTimestamp: Long = 0L
)
