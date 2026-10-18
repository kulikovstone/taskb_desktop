plugins {
    kotlin("jvm") version "2.4.20"
    id("org.jetbrains.compose") version "1.12.2"
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"
}

group = "com.mkulikov.taskb"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material:material:1.12.2")
    val materialSymbolsVersion = "2.2.1"
    implementation("com.composables:icons-material-symbols-outlined-cmp:$materialSymbolsVersion")
    implementation("com.composables:icons-material-symbols-rounded-cmp:$materialSymbolsVersion")
    implementation("com.composables:icons-material-symbols-sharp-cmp:$materialSymbolsVersion")
    implementation("com.composables:icons-material-symbols-outlined-filled-cmp:$materialSymbolsVersion")
    implementation("com.composables:icons-material-symbols-rounded-filled-cmp:$materialSymbolsVersion")
    implementation("com.composables:icons-material-symbols-sharp-filled-cmp:$materialSymbolsVersion")
    implementation("org.xerial:sqlite-jdbc:3.53.4.0")
    implementation("org.jetbrains.exposed:exposed-core:1.6.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:1.6.0")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.17")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "com.mkulikov.taskb.MainKt"
        javaHome = javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }.get().metadata.installationPath.asFile.absolutePath

        nativeDistributions {
            packageName = "TaskB"
            packageVersion = "0.1.0"
            modules("java.sql")

            windows {
                iconFile.set(project.file("packaging/icons/taskb.ico"))
            }
            macOS {
                iconFile.set(project.file("packaging/icons/taskb.icns"))
            }
            linux {
                iconFile.set(project.file("src/main/resources/icons/taskb.png"))
            }
        }
    }
}

tasks.register("installLinuxDesktopEntry") {
    group = "compose desktop"
    description = "Builds TaskB and installs its launcher and icon in the current user's application menu."
    dependsOn("createDistributable")
    onlyIf { System.getProperty("os.name").startsWith("Linux") }

    doLast {
        val applicationDirectory = layout.buildDirectory
            .dir("compose/binaries/main/app/TaskB").get().asFile
        val launcher = applicationDirectory.resolve("bin/TaskB")
        val icon = applicationDirectory.resolve("lib/TaskB.png")
        check(launcher.canExecute() && icon.isFile) { "TaskB distribution is missing its launcher or icon" }

        val dataDirectory = providers.environmentVariable("XDG_DATA_HOME").orNull
            ?.takeIf { it.isNotBlank() }
            ?.let { file(it) }
            ?: file("${System.getProperty("user.home")}/.local/share")
        val desktopEntry = dataDirectory.resolve("applications/com.mkulikov.taskb.desktop")
        desktopEntry.parentFile.mkdirs()
        desktopEntry.writeText(
            """
            [Desktop Entry]
            Type=Application
            Name=TaskB
            Comment=Tasks, events and long-term planning
            Exec="${launcher.absolutePath}"
            TryExec=${launcher.absolutePath}
            Icon=${icon.absolutePath}
            Terminal=false
            Categories=Office;Calendar;
            StartupWMClass=com-mkulikov-taskb-MainKt
            """.trimIndent() + "\n"
        )
        logger.lifecycle("Installed TaskB desktop entry: ${desktopEntry.absolutePath}")
    }
}
