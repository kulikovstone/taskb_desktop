package com.mkulikov.taskb

import com.mkulikov.taskb.storage.AppDatabase
import com.mkulikov.taskb.storage.ApplicationSettingsTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert
import java.nio.file.Path

internal class AppSettings(databaseFile: Path = AppPaths.databaseFile()) {
    private val storage = AppDatabase(databaseFile)

    fun hasCompletedWelcome(): Boolean = transaction(storage.database) {
        ApplicationSettingsTable.selectAll()
            .where { ApplicationSettingsTable.key eq WELCOME_COMPLETED }
            .singleOrNull()
            ?.get(ApplicationSettingsTable.value)
            ?.toBooleanStrict() ?: false
    }

    fun completeWelcome() {
        transaction(storage.database) {
            ApplicationSettingsTable.upsert {
                it[key] = WELCOME_COMPLETED
                it[value] = "true"
            }
        }
    }

    private companion object {
        const val WELCOME_COMPLETED = "welcomeCompleted"
    }
}
