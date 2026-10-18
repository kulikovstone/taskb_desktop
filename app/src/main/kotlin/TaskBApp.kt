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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.mkulikov.taskb.feature.hello.helloEntry
import com.mkulikov.taskb.feature.home.homeEntry
import com.mkulikov.taskb.feature.task.taskEntry
import kotlinx.coroutines.CancellationException

@Composable
internal fun TaskBApp(settings: AppSettings) {
    var navigation by remember(settings) { mutableStateOf<AppNavigation?>(null) }
    var initialError by remember(settings) { mutableStateOf<String?>(null) }

    LaunchedEffect(settings) {
        val hasCompletedWelcome = try {
            settings.hasCompletedWelcome()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            initialError = "Не удалось прочитать настройки. Нажмите «Продолжить», чтобы начать работу."
            false
        }
        navigation = AppNavigation(hasCompletedWelcome)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        val currentNavigation = navigation
        if (currentNavigation == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            NavDisplay(
                backStack = currentNavigation.backStack,
                onBack = currentNavigation::back,
                entryProvider = entryProvider<NavKey> {
                    helloEntry(initialError = initialError) {
                        settings.completeWelcome()
                        currentNavigation.completeHello()
                    }
                    homeEntry()
                    taskEntry()
                }
            )
        }
    }
}
