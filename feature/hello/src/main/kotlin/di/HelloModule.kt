package com.mkulikov.taskb.feature.hello.di

import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.feature.hello.navigation.HelloNavEntry
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun helloModule() = module {
    single<INavEntry>(named<HelloNavEntry>()) { HelloNavEntry(get()) }
}
