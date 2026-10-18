package com.mkulikov.taskb.feature.settings

import com.mkulikov.taskb.feature.settings.api.IAppSettings
import com.mkulikov.taskb.feature.settings.api.database.AppSettingsDao
import com.mkulikov.taskb.feature.settings.api.database.ApplicationSetting

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AppSettings(private val settingsDao: Lazy<AppSettingsDao>) : IAppSettings {
    override suspend fun hasCompletedWelcome(): Boolean = withContext(Dispatchers.IO) {
        settingsDao.value.getValue(WELCOME_COMPLETED)
            ?.toBooleanStrict() ?: false
    }

    override suspend fun completeWelcome() = withContext(Dispatchers.IO) {
        settingsDao.value.upsert(ApplicationSetting(WELCOME_COMPLETED, "true"))
    }

    private companion object {
        const val WELCOME_COMPLETED = "welcomeCompleted"
    }
}
