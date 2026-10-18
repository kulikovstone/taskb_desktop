plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

base {
    archivesName.set("core-database")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(libs.koin.core)
    implementation(libs.room.runtime)
    implementation(libs.sqlite.bundled)
    implementation(libs.kotlinx.coroutines.core)
    ksp(libs.room.compiler)
    testImplementation(libs.kotlin.test)
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

tasks.test {
    useJUnitPlatform()
}
