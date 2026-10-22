package com.mkulikov.taskb.core.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

interface INavEntry {
    fun register(scope: EntryProviderScope<NavKey>, navigator: IAppNavigator)
}
