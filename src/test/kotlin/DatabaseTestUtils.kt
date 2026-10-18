package com.mkulikov.taskb

import com.mkulikov.taskb.storage.AppDatabase

internal inline fun <T> AppDatabase.useDatabase(block: (AppDatabase) -> T): T = try {
    block(this)
} finally {
    close()
}
