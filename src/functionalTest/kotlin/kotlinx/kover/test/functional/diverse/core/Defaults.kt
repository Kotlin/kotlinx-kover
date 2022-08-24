/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.diverse.core

internal fun defaultTestTaskName(projectType: ProjectType): String {
    return when (projectType) {
        ProjectType.KOTLIN_JVM -> "test"
        ProjectType.KOTLIN_MULTIPLATFORM -> "jvmTest"
        ProjectType.ANDROID -> "jvmTest"
    }
}

internal fun defaultMergedXmlReport() = "reports/kover/merged/xml/report.xml"
internal fun defaultMergedHtmlReport() = "reports/kover/merged/html"

internal fun defaultXmlReport() = "reports/kover/xml/report.xml"
internal fun defaultHtmlReport() = "reports/kover/html"

internal fun errorsDirectory() = "kover/errors"

internal fun binaryReportsDirectory() = "kover"
