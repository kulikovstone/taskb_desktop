package com.mkulikov.taskb.feature.task.screen.task

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mkulikov.taskb.feature.task.component.GroupTextField
import com.mkulikov.taskb.feature.task.screen.task_editor.TaskEditorIntent
import com.mkulikov.taskb.ui.component.TaskBTopAppBar
import com.mkulikov.taskb.ui.icon.TaskBIcons
import com.mkulikov.taskb.ui.theme.TaskBTheme
import java.util.Locale

@Composable
internal fun TaskSelectionScreen(state: TaskState, editorGroup: Int?, onIntent: (TaskIntent) -> Unit, onEditorIntent: (TaskEditorIntent) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TaskBTopAppBar(if (state.selection == TaskSelection.Filters) "Filters" else "Choose group", { onIntent(TaskIntent.CloseSelection) }, titleSpacing = 4.dp)
        TaskSelectionContent(state, editorGroup, onIntent, onEditorIntent, Modifier.weight(1f).padding(start = 24.dp, end = 24.dp, top = 24.dp))
    }
}

@Composable
internal fun TaskSelectionDialog(state: TaskState, editorGroup: Int?, onIntent: (TaskIntent) -> Unit, onEditorIntent: (TaskEditorIntent) -> Unit, availableHeight: Dp) {
    Dialog({ onIntent(TaskIntent.CloseSelection) }, properties = DialogProperties(usePlatformDefaultWidth = false, scrimColor = TaskBTheme.colors.scrim)) {
        Surface(shape = RoundedCornerShape(10.dp), color = TaskBTheme.colors.surface, elevation = 4.dp) {
            Column(Modifier.width(480.dp).height(minOf(if (state.selection == TaskSelection.Filters) 496.dp else 624.dp, (availableHeight - 48.dp).coerceAtLeast(160.dp))).padding(24.dp)) {
                Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (state.selection == TaskSelection.Filters) "Filters" else "Choose group", style = TaskBTheme.typography.title, color = TaskBTheme.colors.heading, modifier = Modifier.weight(1f))
                    IconButton({ onIntent(TaskIntent.CloseSelection) }) { Icon(TaskBIcons.Clear, "Close selection", tint = TaskBTheme.colors.muted) }
                }
                Spacer(Modifier.height(16.dp))
                TaskSelectionContent(state, editorGroup, onIntent, onEditorIntent, Modifier.weight(1f), scrollbar = true)
            }
        }
    }
}

@Composable
private fun TaskSelectionContent(state: TaskState, editorGroup: Int?, onIntent: (TaskIntent) -> Unit, onEditorIntent: (TaskEditorIntent) -> Unit, modifier: Modifier, scrollbar: Boolean = false) {
    val filters = state.selection == TaskSelection.Filters
    Column(modifier) {
        if (!filters) {
            GroupTextField(state.groupQuery, { onIntent(TaskIntent.SearchGroups(it)) }, "Search groups", placeholder = "Search groups", search = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
        }
        val scroll = rememberLazyListState()
        Box(Modifier.weight(1f).fillMaxWidth().selectableGroup()) {
            LazyColumn(state = scroll, modifier = Modifier.fillMaxSize().padding(end = if (scrollbar) 20.dp else 0.dp)) {
                if (filters) items(TaskFilter.entries, key = { it.name }) { filter ->
                    SelectionRow(filter.label, state.filter == filter, { onIntent(TaskIntent.Filter(filter)) })
                } else {
                    val query = state.groupQuery.trim()
                    fun choose(id: Int?) {
                        if (state.selection == TaskSelection.EditorGroup) {
                            onEditorIntent(TaskEditorIntent.GroupChanged(id))
                            onIntent(TaskIntent.CloseSelection)
                        } else onIntent(TaskIntent.FilterGroup(id?.let(TaskGroupFilter::Selected) ?: TaskGroupFilter.NoGroup))
                    }
                    val selectedId = if (state.selection == TaskSelection.EditorGroup) editorGroup else (state.groupFilter as? TaskGroupFilter.Selected)?.id
                    if ("No group".contains(query, true)) item(key = "none") {
                        SelectionRow("No group", if (state.selection == TaskSelection.EditorGroup) editorGroup == null else state.groupFilter == TaskGroupFilter.NoGroup,
                            { choose(null) }, groupColor = TaskBTheme.colors.muted)
                    }
                    val groups = state.groups.filter { it.name.contains(query, true) }.sortedBy { it.name.lowercase(Locale.ROOT) }
                    items(groups, key = { it.id }) { group -> SelectionRow(group.name, selectedId == group.id, { choose(group.id) }, group.color.color) }
                    if (groups.isEmpty() && !"No group".contains(query, true)) item { Text("No groups match your search.", color = TaskBTheme.colors.muted, modifier = Modifier.padding(vertical = 24.dp)) }
                }
            }
            if (scrollbar && (scroll.canScrollForward || scroll.canScrollBackward)) VerticalScrollbar(
                rememberScrollbarAdapter(scroll),
                Modifier.align(Alignment.CenterEnd).fillMaxHeight().background(TaskBTheme.colors.border.copy(alpha = 0.5f), RoundedCornerShape(2.dp)),
                style = LocalScrollbarStyle.current.copy(thickness = 4.dp, shape = RoundedCornerShape(2.dp),
                    unhoverColor = TaskBTheme.colors.muted.copy(alpha = 0.35f), hoverColor = TaskBTheme.colors.muted.copy(alpha = 0.65f)),
            )
        }
    }
}

@Composable
private fun SelectionRow(label: String, selected: Boolean, onClick: () -> Unit, groupColor: androidx.compose.ui.graphics.Color? = null) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(63.dp).selectable(selected, role = Role.RadioButton, onClick = onClick).padding(start = 20.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (groupColor != null) Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) { Icon(TaskBIcons.FolderFilled, null, tint = groupColor, modifier = Modifier.size(24.dp)) }
            Text(label, style = TaskBTheme.typography.section, color = TaskBTheme.colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (selected) Icon(TaskBIcons.Done, null, tint = TaskBTheme.colors.accent, modifier = Modifier.size(24.dp))
        }
        Divider(color = TaskBTheme.colors.border)
    }
}
