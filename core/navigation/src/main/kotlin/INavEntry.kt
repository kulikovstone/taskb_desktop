package com.mkulikov.taskb.core.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategy
import androidx.compose.ui.unit.Dp

interface INavEntry {
    fun register(scope: EntryProviderScope<NavKey>, navigator: IAppNavigator)

    fun sceneStrategy(availableWidth: Dp): SceneStrategy<NavKey>? = null
}
