plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
}

base {
    archivesName.set("feature-hello")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":core:navigation"))
    implementation(project(":feature:home:api"))
    implementation(project(":feature:hello:api"))
    implementation(project(":feature:settings:api"))
    implementation(libs.koin.core)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.kotlinx.coroutines.core)
}
