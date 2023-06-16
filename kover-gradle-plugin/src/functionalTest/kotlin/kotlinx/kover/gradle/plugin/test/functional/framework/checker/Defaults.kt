/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.checker

import kotlinx.kover.gradle.plugin.commons.*

internal fun defaultTestTaskName(projectType: KotlinPluginType): String {
    return when (projectType) {
        KotlinPluginType.JVM -> "test"
        KotlinPluginType.MULTIPLATFORM -> "jvmTest"
        KotlinPluginType.ANDROID -> "jvmTest"
    }
}

internal const val defaultReportsDir = "reports/kover"

internal fun defaultXmlReport() = "$defaultReportsDir/report.xml"

internal fun errorsDirectory() = "kover/errors"

internal const val binReportsDirectory = "kover/bin-reports"

internal const val verificationErrorFile = "reports/kover/verify.err"
