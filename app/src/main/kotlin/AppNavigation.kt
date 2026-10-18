package com.mkulikov.taskb

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavKey
import com.mkulikov.taskb.feature.hello.api.HelloRoute
import com.mkulikov.taskb.feature.home.api.HomeRoute

internal class AppNavigation(hasCompletedWelcome: Boolean) {
    val backStack = mutableStateListOf<NavKey>(if (hasCompletedWelcome) HomeRoute else HelloRoute)

    fun completeHello() {
        Snapshot.withMutableSnapshot {
            backStack.clear()
            backStack.add(HomeRoute)
        }
    }

    fun back() {
        if (backStack.size > 1) backStack.removeLast()
    }
}
