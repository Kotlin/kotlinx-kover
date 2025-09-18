plugins {
    kotlin("jvm") version ("2.2.0")
    id("org.jetbrains.kotlinx.kover")
}

kover {
    reports {
        verify {
            rule {
                minBound(50)
            }
        }
    }
}
