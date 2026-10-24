package com.mkulikov.taskb.ui.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkulikov.taskb.ui.theme.TaskBTheme

@Composable
fun TaskBButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 58.dp, minHeight = 40.dp),
        enabled = enabled,
        shape = CircleShape,
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = TaskBTheme.colors.actionFill,
            contentColor = TaskBTheme.colors.onAction,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.button.copy(lineHeight = 20.sp, letterSpacing = 0.1.sp),
        )
    }
}

@Preview(name = "Light", group = "TaskBButton")
@Composable
private fun TaskBButtonLightPreview() {
    TaskBButtonPreview(darkTheme = false)
}

@Preview(name = "Dark", group = "TaskBButton")
@Composable
private fun TaskBButtonDarkPreview() {
    TaskBButtonPreview(darkTheme = true)
}

@Composable
private fun TaskBButtonPreview(darkTheme: Boolean) {
    TaskBTheme(darkTheme = darkTheme) {
        Surface(color = TaskBTheme.colors.surface) {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TaskBButton(label = "Create", onClick = {})
                TaskBButton(label = "Create", onClick = {}, enabled = false)
            }
        }
    }
}
