package com.mkulikov.taskb.feature.task.di

import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.feature.task.navigation.TaskNavEntry
import com.mkulikov.taskb.core.database.AppDatabase
import com.mkulikov.taskb.feature.task.repository.GroupRepository
import com.mkulikov.taskb.feature.task.repository.IGroupRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun taskModule() = module {
    single<IGroupRepository> { GroupRepository(lazy { get<AppDatabase>().groupDao() }) }
    single<INavEntry>(named<TaskNavEntry>()) { TaskNavEntry() }
}
