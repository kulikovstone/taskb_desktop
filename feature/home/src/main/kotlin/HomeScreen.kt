package com.mkulikov.taskb.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun HomeScreen(
    appVersion: String,
    onTasksClick: () -> Unit,
    onGroupsClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(24.dp).widthIn(max = 240.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(onClick = onTasksClick, modifier = Modifier.fillMaxWidth()) {
                Text("Задачи")
            }
            Button(onClick = onGroupsClick, modifier = Modifier.fillMaxWidth()) {
                Text("Группы")
            }
        }
        Text(
            text = "Версия $appVersion",
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
        )
    }
}
