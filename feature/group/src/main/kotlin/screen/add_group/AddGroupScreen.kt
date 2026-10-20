package com.mkulikov.taskb.feature.group.screen.add_group

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.group.component.GroupColorBlock
import com.mkulikov.taskb.feature.group.component.GroupNameField
import com.mkulikov.taskb.ui.component.TaskBTopAppBar
import com.mkulikov.taskb.ui.dialog.TaskBMessageDialog
import com.mkulikov.taskb.ui.icon.TaskBIcons

@Composable
internal fun AddGroupScreen(state: AddGroupState, onIntent: (AddGroupIntent) -> Unit, onBack: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TaskBTopAppBar(title = "New group", onBack = onBack, backEnabled = !state.isSaving) {
                IconButton(onClick = { onIntent(AddGroupIntent.Save) }, enabled = state.canSave) {
                    Icon(TaskBIcons.Done, contentDescription = "Save group")
                }
            }
        },
    ) { padding ->
        if (state.isSaving) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(8.dp))
                GroupNameField(
                    name = state.name,
                    onNameChange = { onIntent(AddGroupIntent.NameChanged(it)) },
                    onSave = { onIntent(AddGroupIntent.Save) },
                    modifier = Modifier.focusRequester(focusRequester),
                )
                Spacer(Modifier.height(30.dp))
                GroupColorBlock(selectedColor = state.color, onSelect = { onIntent(AddGroupIntent.ColorChanged(it)) })
                Spacer(Modifier.height(50.dp))
            }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        }
    }
    when (val dialog = state.dialog) {
        AddGroupDialog.NameConflict -> TaskBMessageDialog(
            title = "Name Conflict",
            message = "Group with this name already exists.",
            onDismiss = { onIntent(AddGroupIntent.DismissDialog) },
        )
        is AddGroupDialog.Error -> TaskBMessageDialog(
            title = "Group error",
            message = dialog.message,
            onDismiss = { onIntent(AddGroupIntent.DismissDialog) },
        )
        null -> Unit
    }
}
