package com.mkulikov.taskb

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun TaskBApp(settings: AppSettings = remember { AppSettings() }) {
    var hasCompletedWelcome by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isContinuing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(settings) {
        try {
            hasCompletedWelcome = withContext(Dispatchers.IO) { settings.hasCompletedWelcome() }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            error = "Не удалось прочитать настройки. Нажмите «Продолжить», чтобы начать работу."
        } finally {
            isLoading = false
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            hasCompletedWelcome -> HomeScreen()
            else -> WelcomeScreen(
                isContinuing = isContinuing,
                error = error,
                onContinue = {
                    isContinuing = true
                    error = null
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                settings.completeWelcome()
                            }
                            hasCompletedWelcome = true
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (_: Exception) {
                            error = "Не удалось сохранить настройки. Попробуйте ещё раз."
                        } finally {
                            isContinuing = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun WelcomeScreen(isContinuing: Boolean, error: String?, onContinue: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 480.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Добро пожаловать в TaskB",
                style = MaterialTheme.typography.h4,
                textAlign = TextAlign.Center
            )
            Button(onClick = onContinue, enabled = !isContinuing) {
                if (isContinuing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Продолжить", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
            }
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize())
}
