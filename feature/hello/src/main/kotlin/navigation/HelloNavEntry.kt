package com.mkulikov.taskb.feature.hello.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.mkulikov.taskb.core.navigation.IAppNavigator
import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.core.navigation.rememberNavEntryResource
import com.mkulikov.taskb.feature.hello.screen.hello.HelloScreen
import com.mkulikov.taskb.feature.hello.api.HelloRoute
import com.mkulikov.taskb.feature.hello.screen.hello.HelloStore
import com.mkulikov.taskb.feature.home.api.HomeRoute
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf

internal class HelloNavEntry : INavEntry {
    override fun register(scope: EntryProviderScope<NavKey>, navigator: IAppNavigator) {
        scope.entry<HelloRoute> { route ->
            val koin = getKoin()
            val store = rememberNavEntryResource(HelloStore::class) {
                koin.get<HelloStore> { parametersOf(route) }
            }
            val state by store.state.collectAsState()

            LaunchedEffect(store, state.isCompleted) {
                if (state.isCompleted) navigator.replaceAll(HomeRoute)
            }

            HelloScreen(state = state, onIntent = store::accept)
        }
    }
}
