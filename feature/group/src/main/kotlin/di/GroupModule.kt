package com.mkulikov.taskb.feature.group.di

import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.feature.group.navigation.GroupNavEntry
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun groupModule() = module {
    single<INavEntry>(named<GroupNavEntry>()) { GroupNavEntry() }
}
