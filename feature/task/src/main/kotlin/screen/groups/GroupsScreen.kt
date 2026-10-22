package com.mkulikov.taskb.feature.task.screen.groups

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.core.navigation.rememberNavEntryResource
import com.mkulikov.taskb.feature.task.repository.IGroupRepository
import com.mkulikov.taskb.feature.task.screen.group_editor.GroupEditorCompactScreen
import com.mkulikov.taskb.feature.task.screen.group_editor.GroupEditorDialogs
import com.mkulikov.taskb.feature.task.screen.group_editor.GroupEditorIntent
import com.mkulikov.taskb.feature.task.screen.group_editor.GroupEditorStore
import org.koin.compose.koinInject

/** Width selects whole layouts. Navigation scenes are deliberately not involved. */
@Composable
internal fun GroupsScreen(onBack: () -> Unit) {
    val repository = koinInject<IGroupRepository>()
    val store = rememberNavEntryResource("task-groups") { GroupsStore(repository) }
    val state by store.state.collectAsState()
    val target = state.editor
    // Keep the editor above the width branch: a resize never recreates its store or draft.
    val editorStore = remember(store, target) {
        target?.let { GroupEditorStore(repository, (it as? GroupEditorTarget.Edit)?.id) }
    }
    DisposableEffect(editorStore) { onDispose { editorStore?.close() } }
    val editorState = editorStore?.state?.collectAsState()?.value
    val onEditorIntent: (GroupEditorIntent) -> Unit = { editorStore?.accept(it) }

    LaunchedEffect(editorState?.isFinished) {
        if (editorState?.isFinished == true) store.accept(GroupsIntent.CloseEditor)
    }
    Surface(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            when {
                maxWidth >= 840.dp -> GroupsWideScreen(state, store::accept, editorState, onEditorIntent, onBack)
                editorState != null -> GroupEditorCompactScreen(editorState, onEditorIntent) { store.accept(GroupsIntent.CloseEditor) }
                else -> GroupsCompactScreen(state, store::accept, onBack)
            }
        }
    }
    if (editorState != null) GroupEditorDialogs(editorState, onEditorIntent)
}
