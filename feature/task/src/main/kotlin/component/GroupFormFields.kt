package com.mkulikov.taskb.feature.task.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.task.model.GroupColor
import com.mkulikov.taskb.feature.task.model.GroupTaskSummary
import com.mkulikov.taskb.ui.icon.TaskBIcons
import com.mkulikov.taskb.ui.theme.Palette
import com.mkulikov.taskb.ui.theme.TaskBTheme

@Composable
internal fun GroupColorPicker(selected: GroupColor, onSelect: (GroupColor) -> Unit, enabled: Boolean) {
    val colors = TaskBTheme.colors
    Column(Modifier.fillMaxWidth().selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().height(18.dp)) {
            Text("Color", style = TaskBTheme.typography.label, color = colors.text)
            Spacer(Modifier.weight(1f))
            Text(selected.label, style = TaskBTheme.typography.small, color = colors.muted)
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GroupColor.entries.forEach { color ->
                Box(
                    Modifier.size(24.dp).clip(CircleShape).background(color.color, CircleShape).border(1.dp, colors.border, CircleShape)
                        .selectable(selected == color, enabled = enabled, role = Role.RadioButton, onClick = { onSelect(color) })
                        .semantics { contentDescription = color.label },
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected == color) Icon(TaskBIcons.Done, contentDescription = null, tint = Palette.Black, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
internal fun GroupStatistics(tasks: GroupTaskSummary) {
    val colors = TaskBTheme.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Planned" to tasks.planned, "Completed" to tasks.completed).forEach { (label, value) ->
            Column(
                Modifier.weight(1f).height(78.dp)
                    .background(colors.subtle, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(label, style = TaskBTheme.typography.small, color = colors.secondary, maxLines = 1)
                Text(value.toString(), style = TaskBTheme.typography.number, color = colors.text)
            }
        }
    }
}
