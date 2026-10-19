package com.mkulikov.taskb.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.getKoin
import org.koin.compose.koinInject

@Composable
fun AppRoot(startDestination: NavKey, modifier: Modifier = Modifier) {
    val navigator = koinInject<AppNavigator>()
    val koin = getKoin()
    val entries = remember(koin) { koin.getAll<INavEntry>() }

    remember(navigator, startDestination) {
        navigator.setStartDestination(startDestination)
    }

    NavDisplay(
        backStack = navigator.backStack,
        onBack = navigator::back,
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberEntryResourceNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entries.forEach { it.register(this, navigator) }
        }
    )
}
