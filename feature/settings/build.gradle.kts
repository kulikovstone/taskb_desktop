plugins {
    alias(libs.plugins.kotlin.jvm)
}

base {
    archivesName.set("feature-settings")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":feature:settings:api"))
    implementation(project(":core:database"))
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}
