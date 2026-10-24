plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

base {
    archivesName.set("feature-task")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":core:database"))
    implementation(project(":core:mvi"))
    implementation(project(":core:navigation"))
    implementation(project(":feature:task:api"))
    implementation(libs.koin.core)
    implementation(libs.koin.compose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.toolingPreview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.materialIcons)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.compose.ui.test)
}

tasks.test { useJUnitPlatform() }
