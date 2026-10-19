package com.mkulikov.taskb.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.mkulikov.taskb.core.navigation.IAppNavigator
import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.feature.home.HomeScreen
import com.mkulikov.taskb.feature.home.api.HomeRoute

internal class HomeNavEntry(private val appVersion: String) : INavEntry {
    override fun register(scope: EntryProviderScope<NavKey>, navigator: IAppNavigator) {
        scope.entry<HomeRoute> { HomeScreen(appVersion = appVersion) }
    }
}
