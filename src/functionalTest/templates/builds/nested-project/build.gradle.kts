plugins {
    base
    id("org.jetbrains.kotlinx.kover")
}

repositories { mavenCentral() }

kover {
    isDisabled.set(false)
}

koverMerged {
    enable()
}
