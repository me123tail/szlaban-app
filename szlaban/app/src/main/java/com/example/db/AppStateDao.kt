package com.example.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppStateDao {
    @Query("SELECT * FROM app_state WHERE id = 1 LIMIT 1")
    fun getAppStateFlow(): Flow<AppState?>

    @Query("SELECT * FROM app_state WHERE id = 1 LIMIT 1")
    suspend fun getAppState(): AppState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppState(appState: AppState)
}
