package com.mkulikov.taskb.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.ui.icon.TaskBIcons
import com.mkulikov.taskb.ui.theme.Palette

@Composable
fun ColorSelector(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val hasFill = selected && color.alpha != 0f
    val contentColor = if (hasFill) Palette.Grey.s900 else MaterialTheme.colors.onSurface
    val borderColor = when {
        hasFill -> Color.Transparent
        selected -> MaterialTheme.colors.primary
        else -> MaterialTheme.colors.onSurface.copy(alpha = 0.24f)
    }
    Surface(
        modifier = modifier.height(40.dp).clip(CircleShape)
            .selectable(selected, enabled = enabled, role = Role.RadioButton, onClick = onClick),
        shape = CircleShape,
        color = if (hasFill) color else Color.Transparent,
        contentColor = contentColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(TaskBIcons.Done, contentDescription = null, modifier = Modifier.size(20.dp))
            } else {
                Box(Modifier.size(20.dp).clip(CircleShape).background(color))
            }
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.body2)
        }
    }
}
