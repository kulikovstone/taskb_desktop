package com.mkulikov.taskb

import java.nio.file.Path

internal object AppPaths {
    fun databaseFile(): Path {
        val home = Path.of(System.getProperty("user.home"))
        val directory = when {
            isWindows() -> environmentPath("LOCALAPPDATA")?.resolve("TaskB")
                ?: home.resolve("AppData/Local/TaskB")
            isMac() -> home.resolve("Library/Application Support/TaskB")
            else -> (environmentPath("XDG_DATA_HOME") ?: home.resolve(".local/share")).resolve("taskb")
        }
        return directory.resolve("taskb.db")
    }

    private fun isWindows() = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
    private fun isMac() = System.getProperty("os.name").startsWith("Mac", ignoreCase = true)

    private fun environmentPath(name: String): Path? = System.getenv(name)
        ?.takeIf { it.isNotBlank() }
        ?.let { Path.of(it) }
        ?.takeIf { it.isAbsolute }
}
