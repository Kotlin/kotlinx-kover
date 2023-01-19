/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.checker

import kotlinx.kover.gradle.plugin.commons.*

internal fun defaultTestTaskName(projectType: KotlinPluginType): String {
    return when (projectType) {
        KotlinPluginType.JVM -> "test"
        KotlinPluginType.MULTI_PLATFORM -> "jvmTest"
        KotlinPluginType.ANDROID -> "jvmTest"
    }
}

internal fun defaultXmlReport() = "reports/kover/report.xml"
internal fun defaultHtmlReport() = "reports/kover/html"

internal fun errorsDirectory() = "kover/errors"

internal const val rawReportsDirectory = "kover/raw-reports"

internal const val verificationErrorFile = "reports/kover/verify.err"
