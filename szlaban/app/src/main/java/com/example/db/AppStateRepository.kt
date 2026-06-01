package com.example.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppStateRepository(private val appStateDao: AppStateDao) {
    val appState: Flow<AppState> = appStateDao.getAppStateFlow().map { it ?: AppState() }

    suspend fun getAppState(): AppState {
        return appStateDao.getAppState() ?: AppState()
    }

    suspend fun updateAppState(appState: AppState) {
        appStateDao.insertAppState(appState)
    }

    suspend fun setHash(hash: String?) {
        val current = getAppState()
        updateAppState(current.copy(hash = hash))
    }

    suspend fun incrementErrors(lockoutTime: Long = 0L) {
        val current = getAppState()
        val nextCount = current.errorCount + 1
        val lockout = if (nextCount >= 3) lockoutTime else 0L
        updateAppState(current.copy(errorCount = nextCount, lockoutTimestamp = lockout))
    }

    suspend fun resetErrors() {
        val current = getAppState()
        updateAppState(current.copy(errorCount = 0, lockoutTimestamp = 0L))
    }
}
