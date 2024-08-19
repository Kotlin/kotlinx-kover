plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

repositories {
    mavenCentral()
}
