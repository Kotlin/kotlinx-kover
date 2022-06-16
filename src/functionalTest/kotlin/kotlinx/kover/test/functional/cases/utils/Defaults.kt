package kotlinx.kover.test.functional.cases.utils

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.core.ProjectType


internal fun defaultBinaryReport(engine: CoverageEngineVendor, projectType: ProjectType): String {
    val extension = if (engine == CoverageEngineVendor.INTELLIJ) "ic" else "exec"
    return when (projectType) {
        ProjectType.KOTLIN_JVM -> "kover/test.$extension"
        ProjectType.KOTLIN_MULTIPLATFORM -> "kover/jvmTest.$extension"
        ProjectType.ANDROID -> "kover/jvmTest.$extension"
    }
}

internal fun defaultMergedXmlReport() = "reports/kover/merged/xml/report.xml"
internal fun defaultMergedHtmlReport() = "reports/kover/merged/html"

internal fun defaultXmlReport() = "reports/kover/xml/report.xml"
internal fun defaultHtmlReport() = "reports/kover/html"

internal fun errorsDirectory() = "kover/errors"
