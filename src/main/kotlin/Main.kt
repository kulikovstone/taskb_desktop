package com.mkulikov.taskb

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.jetbrains.skia.Image

fun main() = application {
    val settings = remember { AppSettings() }
    DisposableEffect(settings) {
        onDispose { settings.close() }
    }

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
            TaskBApp(settings)
        }
    }
}
