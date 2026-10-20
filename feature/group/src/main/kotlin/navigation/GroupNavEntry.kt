package com.mkulikov.taskb.feature.group.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.core.navigation.IAppNavigator
import com.mkulikov.taskb.core.navigation.INavEntry
import com.mkulikov.taskb.core.navigation.rememberNavEntryResource
import com.mkulikov.taskb.feature.group.api.GroupRoute
import com.mkulikov.taskb.feature.group.screen.add_group.AddGroupScreen
import com.mkulikov.taskb.feature.group.screen.add_group.AddGroupStore
import com.mkulikov.taskb.feature.group.screen.edit_group.EditGroupScreen
import com.mkulikov.taskb.feature.group.screen.edit_group.EditGroupStore
import com.mkulikov.taskb.feature.group.screen.group_list.GroupListStore
import com.mkulikov.taskb.feature.group.screen.group_list.GroupListScreen
import org.koin.compose.getKoin
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.ContinuationInterceptor

internal class GroupNavEntry : INavEntry {
    override fun sceneStrategy(availableWidth: Dp): SceneStrategy<NavKey> = GroupSceneStrategy(availableWidth >= 840.dp)

    override fun register(scope: EntryProviderScope<NavKey>, navigator: IAppNavigator) {
        scope.entry<GroupRoute>(metadata = groupPaneMetadata(GroupRoute)) {
            val koin = getKoin()
            val store = rememberNavEntryResource(GroupListStore::class) { koin.get<GroupListStore>() }
            val state by store.state.collectAsState()
            val pane = LocalGroupPaneState.current
            fun openDetail(route: GroupInternalRoute) {
                if (pane?.isBusy == true || pane?.detailRoute == route) return
                if (pane?.detailRoute != null) navigator.replaceTop(route) else navigator.navigate(route)
            }
            GroupListScreen(
                state = state,
                onIntent = store::accept,
                onBack = {
                    if (pane != null) navigator.popUpTo(GroupRoute, inclusive = true) else navigator.back()
                },
                onAdd = { openDetail(GroupInternalRoute.Add) },
                onEdit = { openDetail(GroupInternalRoute.Edit(it)) },
                selectedGroupId = (pane?.detailRoute as? GroupInternalRoute.Edit)?.groupId,
                actionsEnabled = pane?.isBusy != true,
            )
        }
        scope.entry<GroupInternalRoute.Add>(metadata = { groupPaneMetadata(it) }) { AddGroupEntry(navigator = navigator) }
        scope.entry<GroupInternalRoute.Edit>(metadata = { groupPaneMetadata(it) }) { EditGroupEntry(groupId = it.groupId, navigator = navigator) }
    }
}

@Composable
private fun AddGroupEntry(navigator: IAppNavigator) {
    val koin = getKoin()
    val context = rememberCoroutineScope().coroutineContext
    val dispatcher = checkNotNull(context[ContinuationInterceptor] as? CoroutineDispatcher)
    val store = rememberNavEntryResource(AddGroupStore::class) {
        koin.get<AddGroupStore> { parametersOf(dispatcher) }
    }
    val state by store.state.collectAsState()
    val pane = LocalGroupPaneState.current
    ReportPaneBusy(state.isSaving)
    LaunchedEffect(store, state.isFinished) { if (state.isFinished) navigator.back() }
    AddGroupScreen(state = state, onIntent = store::accept, onBack = navigator::back, isInPane = pane != null)
}

@Composable
private fun EditGroupEntry(groupId: Int, navigator: IAppNavigator) {
    val koin = getKoin()
    val context = rememberCoroutineScope().coroutineContext
    val dispatcher = checkNotNull(context[ContinuationInterceptor] as? CoroutineDispatcher)
    val store = rememberNavEntryResource(EditGroupStore::class) {
        koin.get<EditGroupStore> { parametersOf(groupId, dispatcher) }
    }
    val state by store.state.collectAsState()
    val pane = LocalGroupPaneState.current
    ReportPaneBusy(state.isSaving || state.isDeleting)
    LaunchedEffect(store, state.isFinished) { if (state.isFinished) navigator.back() }
    EditGroupScreen(state = state, onIntent = store::accept, onBack = navigator::back, isInPane = pane != null)
}

@Composable
private fun ReportPaneBusy(isBusy: Boolean) {
    val pane = LocalGroupPaneState.current
    SideEffect { pane?.isBusy = isBusy }
    DisposableEffect(pane) { onDispose { pane?.isBusy = false } }
}
