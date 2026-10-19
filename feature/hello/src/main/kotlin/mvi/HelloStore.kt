package com.mkulikov.taskb.feature.hello.mvi

import com.mkulikov.taskb.feature.settings.api.IAppSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class HelloStore(
    private val settings: IAppSettings,
    initialError: String? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val messages = Channel<HelloMessage>(Channel.UNLIMITED)
    private val mutableState = MutableStateFlow(HelloState(error = initialError))
    val state = mutableState.asStateFlow()

    init {
        scope.launch {
            for (message in messages) {
                if (message == HelloMessage.Continue) {
                    if (mutableState.value.isContinuing || mutableState.value.isCompleted) continue
                    mutableState.value = reduceHello(mutableState.value, message)
                    saveWelcome()
                } else {
                    mutableState.value = reduceHello(mutableState.value, message)
                }
            }
        }
    }

    fun accept(intent: HelloIntent) {
        when (intent) {
            HelloIntent.Continue -> messages.trySend(HelloMessage.Continue)
        }
    }

    private fun saveWelcome() {
        scope.launch {
            val result = try {
                settings.completeWelcome()
                HelloMessage.Saved
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                HelloMessage.SaveFailed
            }
            messages.send(result)
        }
    }

    override fun close() {
        scope.cancel()
        messages.cancel()
    }
}
