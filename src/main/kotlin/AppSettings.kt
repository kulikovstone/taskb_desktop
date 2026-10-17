package com.mkulikov.taskb

import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.Properties

internal class AppSettings(private val file: Path = defaultSettingsFile()) {
    fun hasCompletedWelcome(): Boolean {
        if (Files.notExists(file)) return false

        val properties = Properties().apply {
            Files.newBufferedReader(file).use { load(it) }
        }
        return properties.getProperty("welcomeCompleted").toBoolean()
    }

    fun completeWelcome() {
        val settingsFile = file.toAbsolutePath()
        Files.createDirectories(settingsFile.parent)
        val temporaryFile = Files.createTempFile(settingsFile.parent, "settings-", ".tmp")
        try {
            val properties = Properties().apply {
                setProperty("welcomeCompleted", "true")
            }
            Files.newBufferedWriter(temporaryFile).use { properties.store(it, "TaskB") }
            try {
                Files.move(temporaryFile, settingsFile, ATOMIC_MOVE, REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporaryFile, settingsFile, REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporaryFile)
        }
    }
}

private fun defaultSettingsFile(): Path {
    val home = Path.of(System.getProperty("user.home"))
    val configDirectory = when {
        System.getProperty("os.name").startsWith("Windows", ignoreCase = true) ->
            environmentPath("APPDATA")?.resolve("TaskB")
                ?: home.resolve("AppData/Roaming/TaskB")
        System.getProperty("os.name").startsWith("Mac", ignoreCase = true) ->
            home.resolve("Library/Application Support/TaskB")
        else -> (environmentPath("XDG_CONFIG_HOME") ?: home.resolve(".config")).resolve("taskb")
    }
    return configDirectory.resolve("settings.properties")
}

private fun environmentPath(name: String): Path? = System.getenv(name)
    ?.takeIf { it.isNotBlank() }
    ?.let { Path.of(it) }
    ?.takeIf { it.isAbsolute }
