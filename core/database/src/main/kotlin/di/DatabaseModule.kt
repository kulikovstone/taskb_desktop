package com.mkulikov.taskb.core.database.di

import com.mkulikov.taskb.core.database.AppDatabase
import com.mkulikov.taskb.core.database.AppSettings
import com.mkulikov.taskb.core.database.DatabasePaths
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.nio.file.Path

fun databaseModule(databaseFile: Path = DatabasePaths.databaseFile()) = module {
    single { AppDatabase.open(databaseFile) } onClose { database -> database?.close() }
    single { get<AppDatabase>().settingsDao() }
    single { AppSettings(inject()) }
}
