package com.mkulikov.taskb.core.navigation.di

import com.mkulikov.taskb.core.navigation.AppNavigator
import com.mkulikov.taskb.core.navigation.IAppNavigator
import org.koin.dsl.module

fun navigationModule() = module {
    single { AppNavigator() }
    single<IAppNavigator> { get<AppNavigator>() }
}
