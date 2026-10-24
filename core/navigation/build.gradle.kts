plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

base {
    archivesName.set("core-navigation")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.compose.ui.toolingPreview)
    api(libs.navigation3.runtime)
    api(libs.koin.core)
    api(libs.compose.ui)
    implementation(libs.compose.runtime)
    api(libs.navigation3.ui)
    implementation(libs.compose.foundation)
    implementation(libs.koin.compose)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}
