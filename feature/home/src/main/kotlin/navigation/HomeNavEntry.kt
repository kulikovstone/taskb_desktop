package com.mkulikov.taskb.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.mkulikov.taskb.core.navigation.IAppNavigator
import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.feature.task.api.GroupsRoute
import com.mkulikov.taskb.feature.home.screen.home.HomeScreen
import com.mkulikov.taskb.feature.home.api.HomeRoute
import com.mkulikov.taskb.feature.task.api.TaskRoute

internal class HomeNavEntry(private val appVersion: String) : INavEntry {
    override fun register(scope: EntryProviderScope<NavKey>, navigator: IAppNavigator) {
        scope.entry<HomeRoute> {
            HomeScreen(
                appVersion = appVersion,
                onTasksClick = { navigator.navigate(TaskRoute) },
                onGroupsClick = { navigator.navigate(GroupsRoute) },
            )
        }
    }
}
