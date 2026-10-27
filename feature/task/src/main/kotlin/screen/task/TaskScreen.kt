package com.mkulikov.taskb.feature.task.screen.task

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
import com.mkulikov.taskb.feature.task.repository.ITaskRepository
import com.mkulikov.taskb.feature.task.screen.task_editor.TaskEditorCompactScreen
import com.mkulikov.taskb.feature.task.screen.task_editor.TaskEditorDialogs
import com.mkulikov.taskb.feature.task.screen.task_editor.TaskEditorIntent
import com.mkulikov.taskb.feature.task.screen.task_editor.TaskEditorStore
import com.mkulikov.taskb.ui.dialog.TaskBMessageDialog
import org.koin.compose.koinInject

@Composable
internal fun TaskScreen(onBack: () -> Unit) {
    val repository = koinInject<ITaskRepository>()
    val groups = koinInject<IGroupRepository>()
    val store = rememberNavEntryResource("tasks") { TaskStore(repository, groups) }
    val state by store.state.collectAsState()
    val target = state.editor
    // Keep drafts and the active selector above the responsive layout branch.
    val editorStore = remember(store, target) {
        target?.let {
            TaskEditorStore(repository, (it as? TaskEditorTarget.Edit)?.id,
                initialDate = when (state.filter) { TaskFilter.Today -> state.today; TaskFilter.Tomorrow -> state.today.plusDays(1); else -> null },
                initialGroup = (state.groupFilter as? TaskGroupFilter.Selected)?.id)
        }
    }
    DisposableEffect(editorStore) { onDispose { editorStore?.close() } }
    val editor = editorStore?.state?.collectAsState()?.value
    val editorIntent: (TaskEditorIntent) -> Unit = { editorStore?.accept(it) }
    LaunchedEffect(editor?.isFinished) {
        if (editor?.isFinished == true) store.accept(TaskIntent.CloseEditor)
    }
    LaunchedEffect(state.groups, state.isLoading, editor?.groupId, editor?.isBusy, editor?.dialog) {
        if (!state.isLoading && editor != null && !editor.isBusy && editor.dialog == null &&
            editor.groupId != null && state.groups.none { it.id == editor.groupId }) {
            editorStore.accept(TaskEditorIntent.GroupChanged(null))
        }
    }
    Surface(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 840.dp
            when {
                !wide && state.selection != null -> TaskSelectionScreen(state, editor?.groupId, store::accept, editorIntent)
                !wide && editor != null -> TaskEditorCompactScreen(editor, state.groups, state.today, editorIntent,
                    onChooseGroup = { store.accept(TaskIntent.Choose(TaskSelection.EditorGroup)) },
                    onClose = { store.accept(TaskIntent.CloseEditor) })
                else -> TaskListScreen(state, store::accept, editor, editorIntent, wide, onBack)
            }
            if (wide && state.selection != null) TaskSelectionDialog(state, editor?.groupId, store::accept, editorIntent, maxHeight)
            if (editor != null) TaskEditorDialogs(editor, state.today, editorIntent, maxHeight)
        }
    }
    state.error?.let { TaskBMessageDialog("Couldn't update task", it) { store.accept(TaskIntent.DismissError) } }
}
