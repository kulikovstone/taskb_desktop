package com.mkulikov.taskb.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkulikov.taskb.ui.icon.TaskBIcons

@Composable
fun TaskBTopAppBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backEnabled: Boolean = true,
    backIcon: ImageVector = TaskBIcons.Back,
    backContentDescription: String = "Back",
    titleSpacing: Dp? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    if (titleSpacing == null) {
        TopAppBar(
            modifier = modifier.height(64.dp),
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.onBackground,
            elevation = 0.dp,
            title = {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.h6.copy(fontSize = 22.sp, fontWeight = FontWeight.Normal),
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack, enabled = backEnabled) {
                    Icon(backIcon, contentDescription = backContentDescription)
                }
            },
            actions = actions,
        )
        return
    }
    TopAppBar(
        modifier = modifier.height(64.dp),
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
        elevation = 0.dp,
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
            IconButton(onClick = onBack, enabled = backEnabled) {
                Icon(backIcon, contentDescription = backContentDescription)
            }
            Spacer(Modifier.width(titleSpacing))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.h6.copy(fontSize = 22.sp, fontWeight = FontWeight.Normal),
            )
            actions()
        }
    }
}
