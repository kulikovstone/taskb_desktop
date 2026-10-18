package com.mkulikov.taskb.di

import com.mkulikov.taskb.AppPaths
import com.mkulikov.taskb.AppSettings
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.nio.file.Path

internal fun appModule(databaseFile: Path = AppPaths.databaseFile()) = module {
    single { AppSettings(databaseFile) } onClose { settings -> settings?.close() }
}
