package com.mkulikov.taskb.feature.group.di

import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.core.database.AppDatabase
import com.mkulikov.taskb.feature.group.screen.group_editor.GroupEditorArgs
import com.mkulikov.taskb.feature.group.screen.group_editor.GroupEditorStore
import com.mkulikov.taskb.feature.group.screen.group_list.GroupListStore
import com.mkulikov.taskb.feature.group.navigation.GroupNavEntry
import com.mkulikov.taskb.feature.group.repository.GroupRepository
import com.mkulikov.taskb.feature.group.repository.RoomGroupRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun groupModule() = module {
    single { get<AppDatabase>().groupDao() }
    single<GroupRepository> { RoomGroupRepository(inject()) }
    factory { GroupListStore(get()) }
    factory { params ->
        GroupEditorStore(
            repository = get(),
            groupId = params.get<GroupEditorArgs>().groupId,
            dispatcher = params.get(),
        )
    }
    single<INavEntry>(named<GroupNavEntry>()) { GroupNavEntry() }
}
