pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "taskb_desktop"

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(
    ":app",
    ":core:database",
    ":core:mvi",
    ":core:navigation",
    ":feature:hello",
    ":feature:hello:api",
    ":feature:home",
    ":feature:home:api",
    ":feature:settings",
    ":feature:settings:api",
    ":feature:task",
    ":feature:task:api",
    ":ui",
)
