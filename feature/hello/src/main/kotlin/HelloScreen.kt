package com.mkulikov.taskb.feature.hello

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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mkulikov.taskb.feature.hello.mvi.HelloIntent
import com.mkulikov.taskb.feature.hello.mvi.HelloState

@Composable
internal fun HelloScreen(state: HelloState, onIntent: (HelloIntent) -> Unit) {
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
            Button(
                onClick = { onIntent(HelloIntent.Continue) },
                enabled = !state.isContinuing && !state.isCompleted
            ) {
                if (state.isContinuing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Продолжить", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
            }
            if (state.error != null) {
                Text(
                    text = state.error.orEmpty(),
                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
