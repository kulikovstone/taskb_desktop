package com.mkulikov.taskb.feature.settings

import com.mkulikov.taskb.core.database.AppDatabase
import com.mkulikov.taskb.core.database.di.databaseModule
import com.mkulikov.taskb.feature.settings.api.IAppSettings
import com.mkulikov.taskb.feature.settings.di.settingsModule
import org.koin.dsl.koinApplication
import java.nio.file.Path

internal inline fun <T> AppDatabase.useDatabase(block: (AppDatabase) -> T): T = try {
    block(this)
} finally {
    close()
}

internal inline fun <T> withSettings(file: Path, block: (IAppSettings) -> T): T {
    val application = koinApplication { modules(databaseModule(file), settingsModule()) }
    return try {
        block(application.koin.get())
    } finally {
        application.close()
    }
}
