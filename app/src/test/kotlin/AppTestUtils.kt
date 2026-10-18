package com.mkulikov.taskb

import com.mkulikov.taskb.di.appModule
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import java.nio.file.Path

internal inline fun <T> withAppKoin(file: Path, block: (KoinApplication) -> T): T {
    val application = koinApplication { modules(appModule(file)) }
    return try {
        block(application)
    } finally {
        application.close()
    }
}
