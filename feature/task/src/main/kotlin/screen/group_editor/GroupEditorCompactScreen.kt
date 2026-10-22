package com.mkulikov.taskb.feature.task.screen.group_editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.ui.component.TaskBTopAppBar

@Composable
internal fun GroupEditorCompactScreen(state: GroupEditorState, onIntent: (GroupEditorIntent) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TaskBTopAppBar(
            title = if (state.isNew) "New group" else "Group", onBack = onBack,
            backEnabled = !state.isSaving && !state.isDeleting,
            titleSpacing = 4.dp,
        )
        GroupEditorForm(state, onIntent, modifier = Modifier.weight(1f).padding(horizontal = 24.dp), autoFocusName = state.isNew)
    }
}
