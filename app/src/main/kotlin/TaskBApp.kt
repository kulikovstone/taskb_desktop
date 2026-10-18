package com.mkulikov.taskb

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.mkulikov.taskb.core.navigation.AppRoot
import com.mkulikov.taskb.feature.hello.api.HelloRoute
import com.mkulikov.taskb.feature.home.api.HomeRoute
import com.mkulikov.taskb.feature.settings.api.IAppSettings
import kotlinx.coroutines.CancellationException

@Composable
internal fun TaskBApp(settings: IAppSettings) {
    var startDestination by remember(settings) { mutableStateOf<NavKey?>(null) }

    LaunchedEffect(settings) {
        var initialError: String? = null
        val hasCompletedWelcome = try {
            settings.hasCompletedWelcome()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            initialError = "Не удалось прочитать настройки. Нажмите «Продолжить», чтобы начать работу."
            false
        }
        startDestination = if (hasCompletedWelcome) HomeRoute else HelloRoute(initialError)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        val destination = startDestination
        if (destination == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AppRoot(startDestination = destination)
        }
    }
}
