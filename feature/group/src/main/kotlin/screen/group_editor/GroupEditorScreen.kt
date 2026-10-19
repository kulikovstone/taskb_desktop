package com.mkulikov.taskb.feature.group.screen.group_editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.group.component.GroupLoadError
import com.mkulikov.taskb.feature.group.model.GroupColor
import com.mkulikov.taskb.ui.button.TaskBActionButton
import com.mkulikov.taskb.ui.component.ColorSelector
import com.mkulikov.taskb.ui.component.TaskBTopAppBar
import com.mkulikov.taskb.ui.dialog.TaskBConfirmationDialog
import com.mkulikov.taskb.ui.dialog.TaskBMessageDialog
import com.mkulikov.taskb.ui.icon.TaskBIcons

@Composable
internal fun GroupEditorScreen(state: GroupEditorState, onIntent: (GroupEditorIntent) -> Unit, onBack: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TaskBTopAppBar(
                title = if (state.isEditing) "Group" else "New group",
                onBack = onBack,
                backEnabled = !state.isSaving && !state.isDeleting,
            ) {
                IconButton(onClick = { onIntent(GroupEditorIntent.Save) }, enabled = state.canSave) {
                    Icon(TaskBIcons.Done, contentDescription = "Save group")
                }
            }
        },
    ) { padding ->
        when {
            state.isBusy -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.loadError != null -> GroupLoadError(state.loadError, { onIntent(GroupEditorIntent.RetryLoad) }, Modifier.padding(padding))
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                ) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { onIntent(GroupEditorIntent.NameChanged(it)) },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        singleLine = true,
                        placeholder = { Text("Enter group name") },
                        trailingIcon = {
                            if (state.name.isNotEmpty()) {
                                IconButton(onClick = { onIntent(GroupEditorIntent.NameChanged("")) }) {
                                    Icon(TaskBIcons.Clear, contentDescription = "Clear name")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, capitalization = KeyboardCapitalization.Sentences),
                        keyboardActions = KeyboardActions(onDone = { onIntent(GroupEditorIntent.Save) }),
                    )
                    Spacer(Modifier.height(30.dp))
                    SectionTitle("Color")
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        GroupColor.entries.forEach { color ->
                            ColorSelector(
                                label = color.label,
                                color = color.color,
                                selected = state.color == color,
                                onClick = { onIntent(GroupEditorIntent.ColorChanged(color)) },
                            )
                        }
                    }
                    if (state.isEditing) {
                        Spacer(Modifier.height(30.dp))
                        SectionTitle("Statistics")
                        Spacer(Modifier.height(10.dp))
                        Text("No tasks in this group yet.", style = MaterialTheme.typography.body2)
                        Spacer(Modifier.height(30.dp))
                        SectionTitle("Action")
                        Spacer(Modifier.height(10.dp))
                        TaskBActionButton(
                            label = "Delete",
                            icon = TaskBIcons.Delete,
                            onClick = { onIntent(GroupEditorIntent.PrepareDelete) },
                        )
                    }
                    Spacer(Modifier.height(50.dp))
                }
                LaunchedEffect(Unit) { if (!state.isEditing) focusRequester.requestFocus() }
            }
        }
    }
    when (val dialog = state.dialog) {
        GroupEditorDialog.NameConflict -> TaskBMessageDialog(
            title = "Name Conflict",
            message = "Group with this name already exists.",
            onDismiss = { onIntent(GroupEditorIntent.DismissDialog) },
        )
        GroupEditorDialog.ConfirmDelete -> TaskBConfirmationDialog(
            title = "Delete group",
            message = "The group will be deleted.",
            confirmText = "Delete",
            onConfirm = { onIntent(GroupEditorIntent.ConfirmDelete) },
            onDismiss = { onIntent(GroupEditorIntent.DismissDialog) },
        )
        is GroupEditorDialog.Error -> TaskBMessageDialog(
            title = "Group error",
            message = dialog.message,
            onDismiss = { onIntent(GroupEditorIntent.DismissDialog) },
        )
        null -> Unit
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium)
}
