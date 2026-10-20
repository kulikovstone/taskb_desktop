package com.mkulikov.taskb.feature.group.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.group.model.GroupColor
import com.mkulikov.taskb.ui.component.ColorSelector

@Composable
internal fun GroupColorBlock(selectedColor: GroupColor, onSelect: (GroupColor) -> Unit) {
    GroupSectionTitle("Color")
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
                selected = selectedColor == color,
                onClick = { onSelect(color) },
            )
        }
    }
}

@Composable
internal fun GroupSectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium)
}
