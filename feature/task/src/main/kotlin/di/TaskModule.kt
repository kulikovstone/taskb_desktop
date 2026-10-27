package com.mkulikov.taskb.feature.task.di

import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.feature.task.navigation.TaskNavEntry
import com.mkulikov.taskb.core.database.AppDatabase
import com.mkulikov.taskb.feature.task.repository.GroupRepository
import com.mkulikov.taskb.feature.task.repository.IGroupRepository
import com.mkulikov.taskb.feature.task.repository.ITaskRepository
import com.mkulikov.taskb.feature.task.repository.TaskRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun taskModule() = module {
    single<IGroupRepository> { GroupRepository(lazy { get<AppDatabase>().groupDao() }) }
    single<ITaskRepository> { TaskRepository(lazy { get<AppDatabase>().taskDao() }) }
    single<INavEntry>(named<TaskNavEntry>()) { TaskNavEntry() }
}
