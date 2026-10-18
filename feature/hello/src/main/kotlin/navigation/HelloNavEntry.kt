package com.mkulikov.taskb.feature.hello.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.mkulikov.taskb.core.database.AppSettings
import com.mkulikov.taskb.core.navigation.IAppNavigator
import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.feature.hello.HelloScreen
import com.mkulikov.taskb.feature.hello.api.HelloRoute
import com.mkulikov.taskb.feature.home.api.HomeRoute

internal class HelloNavEntry(private val settings: AppSettings) : INavEntry {
    override fun register(scope: EntryProviderScope<NavKey>, navigator: IAppNavigator) {
        scope.entry<HelloRoute> { route ->
            HelloScreen(initialError = route.initialError) {
                settings.completeWelcome()
                navigator.replaceAll(HomeRoute)
            }
        }
    }
}
