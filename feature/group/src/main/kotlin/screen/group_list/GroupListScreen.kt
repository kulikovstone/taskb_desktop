package com.mkulikov.taskb.feature.group.screen.group_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.group.component.GroupLoadError
import com.mkulikov.taskb.feature.group.model.Group
import com.mkulikov.taskb.ui.component.TaskBTopAppBar
import com.mkulikov.taskb.ui.icon.TaskBIcons

@Composable
internal fun GroupListScreen(
    state: GroupListState,
    onIntent: (GroupListIntent) -> Unit,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    selectedGroupId: Int? = null,
    actionsEnabled: Boolean = true,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = MaterialTheme.colors.background,
        topBar = {
            TaskBTopAppBar(title = "Groups", onBack = onBack, backEnabled = actionsEnabled) {
                IconButton(onClick = onAdd, enabled = actionsEnabled) { Icon(TaskBIcons.Add, contentDescription = "Add group") }
            }
        },
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> GroupLoadError(state.error, { onIntent(GroupListIntent.Retry) }, Modifier.padding(padding))
            state.groups.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No groups yet.", color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f))
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).selectableGroup(),
                contentPadding = PaddingValues(bottom = 50.dp),
            ) {
                items(state.groups, key = { it.id }) { group ->
                    GroupItem(group, selected = group.id == selectedGroupId, enabled = actionsEnabled, onClick = { onEdit(group.id) })
                }
            }
        }
    }
}

@Composable
private fun GroupItem(group: Group, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (selected) MaterialTheme.colors.primary.copy(alpha = 0.12f) else Color.Transparent)
            .selectable(selected = selected, enabled = enabled, role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(10.dp).height(60.dp).background(group.color.color))
        Text(
            text = group.name,
            modifier = Modifier.weight(1f).padding(start = 15.dp, end = 16.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
