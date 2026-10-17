package com.mkulikov.taskb

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.jetbrains.skia.Image

fun main() = application {
    val appIcon = remember {
        val iconBytes = checkNotNull(
            Thread.currentThread().contextClassLoader.getResourceAsStream("icons/taskb.png")
        ) { "Application icon resource not found: icons/taskb.png" }.use { it.readBytes() }
        BitmapPainter(Image.makeFromEncoded(iconBytes).toComposeImageBitmap())
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "TaskB",
        icon = appIcon,
        state = rememberWindowState(width = 960.dp, height = 640.dp)
    ) {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("TaskB", style = MaterialTheme.typography.h4)
                }
            }
        }
    }
}
