package com.mkulikov.taskb.feature.settings.di

import com.mkulikov.taskb.core.database.AppDatabase
import com.mkulikov.taskb.feature.settings.AppSettings
import com.mkulikov.taskb.feature.settings.api.IAppSettings
import org.koin.dsl.module

fun settingsModule() = module {
    single { get<AppDatabase>().settingsDao() }
    single<IAppSettings> { AppSettings(inject()) }
}
