/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.checker

import kotlinx.kover.test.functional.framework.common.*

internal fun defaultTestTaskName(projectType: KotlinPluginType): String {
    return when (projectType) {
        KotlinPluginType.JVM -> "test"
        KotlinPluginType.MULTIPLATFORM -> "jvmTest"
        KotlinPluginType.ANDROID -> "jvmTest"
    }
}

internal fun defaultMergedXmlReport() = "reports/kover/merged/xml/report.xml"
internal fun defaultMergedHtmlReport() = "reports/kover/merged/html"

internal fun defaultXmlReport() = "reports/kover/xml/report.xml"
internal fun defaultHtmlReport() = "reports/kover/html"

internal fun errorsDirectory() = "kover/errors"

internal const val binaryReportsDirectory = "kover"

internal const val verificationErrorFile = "reports/kover/verification/errors.txt"
