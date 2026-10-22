package com.mkulikov.taskb.feature.task.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.ui.theme.TaskBTheme
import java.util.Locale

@Composable
internal fun GroupButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    val colors = TaskBTheme.colors
    val foreground = if (destructive) colors.danger else if (enabled) colors.onAction else colors.muted
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        border = if (!enabled && !destructive) BorderStroke(1.dp, colors.border) else null,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (destructive) Color.Transparent else colors.actionFill,
            contentColor = foreground, disabledBackgroundColor = if (destructive) Color.Transparent else colors.surface,
            disabledContentColor = colors.muted.copy(alpha = 0.5f),
        ),
        elevation = null,
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Icon(icon, contentDescription = null, tint = foreground.copy(alpha = if (enabled) 1f else 0.5f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = TaskBTheme.typography.button, maxLines = 1)
    }
}

@Composable
internal fun GroupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    description: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    search: Boolean = false,
    enabled: Boolean = true,
    autoFocus: Boolean = false,
    onDone: () -> Unit = {},
) {
    val colors = TaskBTheme.colors
    var focused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    Row(
        modifier = modifier.height(44.dp).onFocusChanged { focused = it.hasFocus }.focusGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = TaskBTheme.typography.body.copy(color = colors.text),
            cursorBrush = SolidColor(colors.accent),
            keyboardOptions = KeyboardOptions(imeAction = if (search) ImeAction.Search else ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier.weight(1f).height(44.dp).focusRequester(focusRequester)
                .border(if (focused) 2.dp else 1.dp, if (focused) colors.accent else colors.border, RoundedCornerShape(8.dp))
                .semantics { contentDescription = description },
            decorationBox = { inner ->
                Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (search) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) Text(placeholder, color = colors.muted, style = TaskBTheme.typography.body)
                        inner()
                    }
                }
            },
        )
        if (focused && value.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(32.dp).clickable(
                    enabled = enabled,
                    interactionSource = null,
                    indication = null,
                    role = Role.Button,
                ) {
                    onValueChange("")
                    focusRequester.requestFocus()
                },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = "Clear ${description.lowercase(Locale.ROOT)}",
                    tint = colors.clearIcon,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
    LaunchedEffect(autoFocus) {
        if (autoFocus) focusRequester.requestFocus()
    }
}

@Composable
internal fun GroupLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(28.dp)) }
}

@Composable
internal fun GroupLoadError(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(message, color = TaskBTheme.colors.muted, style = TaskBTheme.typography.body)
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}
