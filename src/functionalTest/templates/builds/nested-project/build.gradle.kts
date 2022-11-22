plugins {
    base
    id("org.jetbrains.kotlinx.kover") version("SNAPSHOT")
}

repositories { mavenCentral() }

kover {
    isDisabled.set(false)
}

koverMerged {
    enable()
}
