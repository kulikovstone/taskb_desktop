package com.mkulikov.taskb.feature.hello.di

import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.feature.hello.api.HelloRoute
import com.mkulikov.taskb.feature.hello.screen.hello.HelloStore
import com.mkulikov.taskb.feature.hello.navigation.HelloNavEntry
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun helloModule() = module {
    factory { (route: HelloRoute) -> HelloStore(settings = get(), initialError = route.initialError) }
    single<INavEntry>(named<HelloNavEntry>()) { HelloNavEntry() }
}
