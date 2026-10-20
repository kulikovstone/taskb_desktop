package com.mkulikov.taskb.feature.group.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

@Stable
internal class GroupPaneState(val detailRoute: GroupInternalRoute?) {
    var isBusy by mutableStateOf(false)
}

internal val LocalGroupPaneState = staticCompositionLocalOf<GroupPaneState?> { null }
