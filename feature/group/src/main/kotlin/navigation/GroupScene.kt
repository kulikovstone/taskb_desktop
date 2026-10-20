package com.mkulikov.taskb.feature.group.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.get
import androidx.navigation3.scene.Scene

internal data class GroupScene(
    val listEntry: NavEntry<NavKey>,
    val detailEntry: NavEntry<NavKey>?,
    override val previousEntries: List<NavEntry<NavKey>>,
) : Scene<NavKey> {
    override val key: Any = listEntry.contentKey
    override val entries: List<NavEntry<NavKey>> = listOfNotNull(listEntry, detailEntry)

    override val content: @Composable () -> Unit = {
        val paneState = remember(detailEntry?.contentKey) {
            GroupPaneState(detailEntry?.metadata?.get(GroupPaneKey) as? GroupInternalRoute)
        }
        CompositionLocalProvider(LocalGroupPaneState provides paneState) {
            Row(Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
                Box(Modifier.width(320.dp).fillMaxHeight()) { listEntry.Content() }
                Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colors.onSurface.copy(alpha = 0.12f)))
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    if (detailEntry != null) {
                        detailEntry.Content()
                    } else {
                        Text(
                            text = "Select a group or create a new one.",
                            modifier = Modifier.padding(32.dp),
                            color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
