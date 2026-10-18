package com.mkulikov.taskb.core.database

internal inline fun <T> AppDatabase.useDatabase(block: (AppDatabase) -> T): T = try {
    block(this)
} finally {
    close()
}
