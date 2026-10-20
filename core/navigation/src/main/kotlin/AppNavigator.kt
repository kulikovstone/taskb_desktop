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

    override fun replaceTop(key: NavKey) {
        if (backStack.isEmpty()) backStack.add(key) else backStack[backStack.lastIndex] = key
    }

    override fun popUpTo(key: NavKey, inclusive: Boolean) {
        val index = backStack.indexOfLast { it == key }
        if (index < 0) return
        val size = (index + if (inclusive) 0 else 1).coerceAtLeast(1)
        Snapshot.withMutableSnapshot {
            while (backStack.size > size) backStack.removeLast()
        }
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
