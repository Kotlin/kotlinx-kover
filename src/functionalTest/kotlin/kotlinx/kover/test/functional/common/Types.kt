/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.common

import java.io.File

internal enum class GradleScriptLanguage { KOTLIN, GROOVY }


internal fun File.extractScriptLanguage(): GradleScriptLanguage {
    return when(name) {
        "build.gradle.kts" -> GradleScriptLanguage.KOTLIN
        "build.gradle" -> GradleScriptLanguage.GROOVY
        else -> throw Exception("File $name is not a Gradle build script file")
    }
}
