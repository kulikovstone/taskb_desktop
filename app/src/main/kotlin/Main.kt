package com.mkulikov.taskb

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mkulikov.taskb.di.appModule
import com.mkulikov.taskb.feature.settings.api.IAppSettings
import com.mkulikov.taskb.ui.theme.TaskBTheme
import org.jetbrains.skia.Image
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

fun main() {
    startKoin {
        modules(appModule())
    }

    try {
        application(exitProcessOnExit = false) {
            val settings = koinInject<IAppSettings>()
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
                state = rememberWindowState(width = 1100.dp, height = 800.dp)
            ) {
                TaskBTheme {
                    TaskBApp(settings)
                }
            }
        }
    } finally {
        stopKoin()
    }
}
