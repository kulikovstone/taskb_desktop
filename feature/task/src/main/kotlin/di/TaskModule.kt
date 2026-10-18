package com.mkulikov.taskb.feature.task.di

import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.feature.task.navigation.TaskNavEntry
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun taskModule() = module {
    single<INavEntry>(named<TaskNavEntry>()) { TaskNavEntry() }
}
