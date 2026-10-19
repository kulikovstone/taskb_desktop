package com.mkulikov.taskb.feature.group.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.mkulikov.taskb.core.navigation.IAppNavigator
import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.feature.group.GroupScreen
import com.mkulikov.taskb.feature.group.api.GroupRoute

internal class GroupNavEntry : INavEntry {
    override fun register(scope: EntryProviderScope<NavKey>, navigator: IAppNavigator) {
        scope.entry<GroupRoute> { GroupScreen() }
    }
}
