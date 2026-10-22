package com.mkulikov.taskb.feature.task.screen.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.task.component.GroupLoadError
import com.mkulikov.taskb.feature.task.component.GroupLoading
import com.mkulikov.taskb.feature.task.model.Group
import com.mkulikov.taskb.ui.icon.TaskBIcons
import com.mkulikov.taskb.ui.theme.TaskBTheme

private val GroupRowHorizontalPadding = 20.dp
private val GroupMarkSize = 28.dp
private val GroupRowSpacing = 12.dp
private val GroupSortHeaderPadding = 16.dp

@Composable
internal fun GroupsFilters(state: GroupsState, onIntent: (GroupsIntent) -> Unit) {
    val colors = TaskBTheme.colors
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(GroupFilter.All to "All  ${state.groups.size}", GroupFilter.WithTasks to "With tasks  ${state.withTasksCount}", GroupFilter.Empty to "Empty ${state.emptyCount}").forEach { (filter, label) ->
            val selected = state.filter == filter
            Column(
                Modifier.width(IntrinsicSize.Max).height(40.dp).selectable(selected, role = Role.Tab, onClick = { onIntent(GroupsIntent.Filter(filter)) }),
            ) {
                Box(Modifier.weight(1f).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                    Text(label, color = if (selected) colors.accent else colors.secondary, style = TaskBTheme.typography.label, maxLines = 1)
                }
                Box(Modifier.fillMaxWidth().height(2.dp).background(if (selected) colors.accent else colors.surface))
            }
        }
    }
}

@Composable
internal fun GroupsDirectory(
    state: GroupsState,
    onIntent: (GroupsIntent) -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = TaskBTheme.colors
    val groups = state.visibleGroups
    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(if (compact) 28.dp else 40.dp), contentAlignment = Alignment.CenterStart) {
            Text(state.summary, color = colors.muted, style = TaskBTheme.typography.section.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal))
        }
        if (compact) Spacer(Modifier.height(16.dp)) else {
            Row(
                Modifier.fillMaxWidth().padding(
                    start = GroupRowHorizontalPadding + GroupMarkSize + GroupRowSpacing - GroupSortHeaderPadding,
                    end = GroupRowHorizontalPadding,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(GroupRowSpacing),
            ) {
                SortHeader("Name", GroupSort.Name, state, onIntent, Modifier.weight(1f))
                SortHeader("Tasks", GroupSort.Tasks, state, onIntent, Modifier.width(96.dp), centered = true)
                Spacer(Modifier.width(20.dp))
            }
            Divider(color = colors.border)
        }
        when {
            state.isLoading -> GroupLoading(Modifier.weight(1f))
            state.loadError != null -> GroupLoadError(state.loadError, { onIntent(GroupsIntent.RetryLoad) })
            groups.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(if (state.groups.isEmpty()) "No groups yet." else "No groups match your search or filter.", style = TaskBTheme.typography.body, color = colors.muted)
            }
            else -> LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(groups, key = { it.id }) { group ->
                    GroupRow(group, selected = state.editor == GroupEditorTarget.Edit(group.id), compact = compact, enabled = enabled,
                        showDivider = !compact || group.id != groups.last().id) {
                        onIntent(GroupsIntent.OpenGroup(group.id))
                    }
                }
            }
        }
    }
}

@Composable
private fun SortHeader(
    label: String,
    sort: GroupSort,
    state: GroupsState,
    onIntent: (GroupsIntent) -> Unit,
    modifier: Modifier,
    centered: Boolean = false,
) {
    val colors = TaskBTheme.colors
    Box(
        modifier = modifier,
        contentAlignment = if (centered) Alignment.Center else Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.clickable(interactionSource = null, indication = null, role = Role.Button) {
                onIntent(GroupsIntent.Sort(sort))
            }.semantics { contentDescription = "Sort by $label" }
                .padding(GroupSortHeaderPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = TaskBTheme.typography.label, color = colors.secondary, maxLines = 1)
            if (state.sort == sort) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    TaskBIcons.Sort, contentDescription = null,
                    tint = if (state.reverseSort) colors.danger else colors.success,
                    modifier = Modifier.size(14.dp).rotate(if (state.reverseSort) 180f else 0f),
                )
            }
        }
    }
}

@Composable
private fun GroupRow(group: Group, selected: Boolean, compact: Boolean, enabled: Boolean, showDivider: Boolean, onClick: () -> Unit) {
    val colors = TaskBTheme.colors
    Column(Modifier.fillMaxWidth().background(if (selected) colors.selected else colors.surface)
        .selectable(selected, enabled = enabled, role = Role.Button, onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth().height(if (showDivider) 63.dp else 64.dp).padding(horizontal = GroupRowHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(GroupRowSpacing),
        ) {
            Box(Modifier.size(GroupMarkSize), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = group.color.color, modifier = Modifier.size(20.dp))
            }
            Text(group.name, modifier = Modifier.weight(1f), color = colors.text, style = TaskBTheme.typography.section, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Box(if (compact) Modifier else Modifier.width(96.dp), contentAlignment = Alignment.Center) {
                val count = group.tasks.planned
                Text(if (compact) "$count ${if (count == 1) "task" else "tasks"}" else count.toString(), color = colors.muted, style = TaskBTheme.typography.body)
            }
            if (!compact) Icon(Icons.AutoMirrored.Default.NavigateNext, contentDescription = null, tint = colors.muted, modifier = Modifier.size(20.dp))
        }
        if (showDivider) Divider(color = colors.border, thickness = 1.dp)
    }
}
