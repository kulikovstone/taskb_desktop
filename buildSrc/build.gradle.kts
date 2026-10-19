plugins { java }

repositories { mavenCentral() }

dependencies {
    implementation(gradleApi())
    testImplementation("junit:junit:4.13.2")
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
