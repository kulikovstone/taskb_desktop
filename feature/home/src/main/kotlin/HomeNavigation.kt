package com.mkulikov.taskb.feature.home

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.mkulikov.taskb.feature.home.api.HomeRoute

fun EntryProviderScope<NavKey>.homeEntry() {
    entry<HomeRoute> { HomeScreen() }
}
