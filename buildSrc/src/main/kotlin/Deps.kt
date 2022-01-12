object Deps {
    object Android {
        const val version = "4.2.2"

        const val gradlePlugin = "com.android.tools.build:gradle:$version"
    }

    object Kotlin {
        const val version = "1.5.31"

        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val compilerEmbeddable = "org.jetbrains.kotlin:kotlin-compiler-embeddable:$version"
        const val compilerRunner = "org.jetbrains.kotlin:kotlin-compiler-runner:$version"
    }
}