package com.mkulikov.taskb.core.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppSettings internal constructor(private val settingsDao: Lazy<AppSettingsDao>) {
    suspend fun hasCompletedWelcome(): Boolean = withContext(Dispatchers.IO) {
        settingsDao.value.getValue(WELCOME_COMPLETED)
            ?.toBooleanStrict() ?: false
    }

    suspend fun completeWelcome() = withContext(Dispatchers.IO) {
        settingsDao.value.upsert(ApplicationSetting(WELCOME_COMPLETED, "true"))
    }

    private companion object {
        const val WELCOME_COMPLETED = "welcomeCompleted"
    }
}
