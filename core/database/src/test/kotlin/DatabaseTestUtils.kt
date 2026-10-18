package com.mkulikov.taskb.core.database

import com.mkulikov.taskb.core.database.di.databaseModule
import org.koin.dsl.koinApplication
import java.nio.file.Path

internal inline fun <T> AppDatabase.useDatabase(block: (AppDatabase) -> T): T = try {
    block(this)
} finally {
    close()
}

internal inline fun <T> withSettings(file: Path, block: (AppSettings) -> T): T {
    val application = koinApplication { modules(databaseModule(file)) }
    return try {
        block(application.koin.get())
    } finally {
        application.close()
    }
}
