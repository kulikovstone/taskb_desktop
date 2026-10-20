package com.mkulikov.taskb.feature.group.navigation

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategyScope
import com.mkulikov.taskb.feature.group.api.GroupRoute
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import org.junit.jupiter.api.Test

class GroupSceneStrategyTest {
    @Test
    fun `narrow layouts and unrelated destinations keep default navigation`() {
        val groupEntries = listOf(entry(GroupRoute), entry(GroupInternalRoute.Add))
        assertNull(with(GroupSceneStrategy(false)) { SceneStrategyScope<NavKey>().calculateScene(groupEntries) })
        val otherEntries = groupEntries + entry(OtherRoute)
        assertNull(with(GroupSceneStrategy(true)) { SceneStrategyScope<NavKey>().calculateScene(otherEntries) })
    }

    @Test
    fun `wide list has a placeholder and back leads out of the feature`() {
        val entries = listOf(entry(OtherRoute), entry(GroupRoute))
        val scene = assertIs<GroupScene>(with(GroupSceneStrategy(true)) { SceneStrategyScope<NavKey>().calculateScene(entries) })
        assertEquals(listOf(entries.last()), scene.entries)
        assertNull(scene.detailEntry)
        assertEquals(entries.dropLast(1), scene.previousEntries)
    }

    @Test
    fun `wide detail shares a scene key with its list and keeps its back target`() {
        val list = entry(GroupRoute)
        val addEntries = listOf(entry(OtherRoute), list, entry(GroupInternalRoute.Add))
        val editEntries = addEntries.dropLast(1) + entry(GroupInternalRoute.Edit(7))
        val add = assertIs<GroupScene>(with(GroupSceneStrategy(true)) { SceneStrategyScope<NavKey>().calculateScene(addEntries) })
        val edit = assertIs<GroupScene>(with(GroupSceneStrategy(true)) { SceneStrategyScope<NavKey>().calculateScene(editEntries) })
        assertEquals(add.key, edit.key)
        assertEquals(listOf(list, editEntries.last()), edit.entries)
        assertEquals(editEntries.dropLast(1), edit.previousEntries)
    }

    private fun entry(key: NavKey) = NavEntry<NavKey>(
        key,
        metadata = if (key == GroupRoute || key is GroupInternalRoute) groupPaneMetadata(key) else emptyMap(),
    ) {}
    private data object OtherRoute : NavKey
}
