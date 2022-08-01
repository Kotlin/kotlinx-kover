/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.cases.utils

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.core.ProjectType


internal fun defaultTestTask(engine: CoverageEngineVendor, projectType: ProjectType): String {
    val extension = if (engine == CoverageEngineVendor.INTELLIJ) "ic" else "exec"
    return when (projectType) {
        ProjectType.KOTLIN_JVM -> "test.$extension"
        ProjectType.KOTLIN_MULTIPLATFORM -> "jvmTest.$extension"
        ProjectType.ANDROID -> "jvmTest.$extension"
    }
}

internal fun defaultBinaryReport(engine: CoverageEngineVendor, projectType: ProjectType): String {
    return "kover/" + defaultTestTask(engine, projectType)
}

internal fun defaultMergedXmlReport() = "reports/kover/merged/xml/report.xml"
internal fun defaultMergedHtmlReport() = "reports/kover/merged/html"

internal fun defaultXmlReport() = "reports/kover/xml/report.xml"
internal fun defaultHtmlReport() = "reports/kover/html"

internal fun errorsDirectory() = "kover/errors"
