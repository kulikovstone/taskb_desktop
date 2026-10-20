package com.mkulikov.taskb.feature.group.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.get
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.mkulikov.taskb.feature.group.api.GroupRoute

internal class GroupSceneStrategy(private val isWide: Boolean) : SceneStrategy<NavKey> {
    override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
        if (!isWide) return null
        val last = entries.lastOrNull() ?: return null
        val route = last.metadata[GroupPaneKey]
        if (route != GroupRoute && route !is GroupInternalRoute) return null
        val listEntry = entries.lastOrNull { it.metadata[GroupPaneKey] == GroupRoute } ?: return null
        return GroupScene(
            listEntry = listEntry,
            detailEntry = last.takeIf { route is GroupInternalRoute },
            previousEntries = entries.dropLast(1),
        )
    }
}
