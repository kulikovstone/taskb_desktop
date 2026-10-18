package com.mkulikov.taskb.feature.home.di

import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.feature.home.navigation.HomeNavEntry
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun homeModule() = module {
    single<INavEntry>(named<HomeNavEntry>()) { HomeNavEntry() }
}
