package com.mkulikov.taskb.feature.settings

import com.mkulikov.taskb.feature.settings.api.IAppSettings
import com.mkulikov.taskb.feature.settings.api.database.IAppSettingsDao
import com.mkulikov.taskb.feature.settings.api.database.ApplicationSetting

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AppSettings(private val settingsDao: Lazy<IAppSettingsDao>) : IAppSettings {
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
