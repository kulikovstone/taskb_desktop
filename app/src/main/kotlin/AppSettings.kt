package com.mkulikov.taskb

import com.mkulikov.taskb.storage.AppDatabase
import com.mkulikov.taskb.storage.ApplicationSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

internal class AppSettings(databaseFile: Path = AppPaths.databaseFile()) : AutoCloseable {
    private val storage = lazy { AppDatabase.open(databaseFile) }

    suspend fun hasCompletedWelcome(): Boolean = withContext(Dispatchers.IO) {
        storage.value.settingsDao().getValue(WELCOME_COMPLETED)
            ?.toBooleanStrict() ?: false
    }

    suspend fun completeWelcome() = withContext(Dispatchers.IO) {
        storage.value.settingsDao().upsert(ApplicationSetting(WELCOME_COMPLETED, "true"))
    }

    override fun close() {
        if (storage.isInitialized()) storage.value.close()
    }

    private companion object {
        const val WELCOME_COMPLETED = "welcomeCompleted"
    }
}
