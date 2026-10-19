plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

base {
    archivesName.set("core-mvi")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
