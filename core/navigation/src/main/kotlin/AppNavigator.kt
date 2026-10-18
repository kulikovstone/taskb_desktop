package com.mkulikov.taskb.core.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavKey

@Stable
internal class AppNavigator : IAppNavigator {
    val backStack = mutableStateListOf<NavKey>()

    fun setStartDestination(key: NavKey) {
        if (backStack.isEmpty()) backStack.add(key)
    }

    override fun navigate(key: NavKey) {
        backStack.add(key)
    }

    override fun replaceAll(key: NavKey) {
        Snapshot.withMutableSnapshot {
            backStack.clear()
            backStack.add(key)
        }
    }

    override fun back() {
        if (backStack.size > 1) backStack.removeLast()
    }
}
