package com.mkulikov.taskb.feature.group.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.metadata

internal object GroupPaneKey : NavMetadataKey<NavKey>

internal fun groupPaneMetadata(route: NavKey) = metadata { put(GroupPaneKey, route) }
