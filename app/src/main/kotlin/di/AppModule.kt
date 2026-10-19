package com.mkulikov.taskb.di

import com.mkulikov.taskb.AppVersion
import com.mkulikov.taskb.core.database.DatabasePaths
import com.mkulikov.taskb.core.database.di.databaseModule
import com.mkulikov.taskb.core.navigation.di.navigationModule
import com.mkulikov.taskb.feature.group.di.groupModule
import com.mkulikov.taskb.feature.hello.di.helloModule
import com.mkulikov.taskb.feature.home.di.homeModule
import com.mkulikov.taskb.feature.settings.di.settingsModule
import com.mkulikov.taskb.feature.task.di.taskModule
import org.koin.dsl.module
import java.nio.file.Path

internal fun appModule(databaseFile: Path = DatabasePaths.databaseFile()) = module {
    includes(
        databaseModule(databaseFile),
        settingsModule(),
        navigationModule(),
        groupModule(),
        helloModule(),
        homeModule(AppVersion.value),
        taskModule(),
    )
}
