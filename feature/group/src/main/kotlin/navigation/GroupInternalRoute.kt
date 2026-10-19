package com.mkulikov.taskb.feature.group.navigation

import androidx.navigation3.runtime.NavKey

internal sealed interface GroupInternalRoute : NavKey {
    data object Add : GroupInternalRoute
    data class Edit(val groupId: Int) : GroupInternalRoute
}
