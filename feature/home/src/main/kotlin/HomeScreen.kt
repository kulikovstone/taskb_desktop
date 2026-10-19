package com.mkulikov.taskb.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun HomeScreen(appVersion: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Главный экран",
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.h4,
        )
        Text(
            text = "Версия $appVersion",
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium),
        )
    }
}
