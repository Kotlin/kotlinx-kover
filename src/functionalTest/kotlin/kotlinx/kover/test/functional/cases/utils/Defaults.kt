package kotlinx.kover.test.functional.cases.utils

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.core.ProjectType


internal fun defaultBinaryReport(engine: CoverageEngine, projectType: ProjectType): String {
    val extension = if (engine == CoverageEngine.INTELLIJ) "ic" else "exec"
    return when (projectType) {
        ProjectType.KOTLIN_JVM -> "kover/test.$extension"
        ProjectType.KOTLIN_MULTIPLATFORM -> "kover/jvmTest.$extension"
        ProjectType.ANDROID -> "kover/jvmTest.$extension"
    }
}

internal fun defaultSmapFile(projectType: ProjectType): String {
    return defaultBinaryReport(CoverageEngine.INTELLIJ, projectType) + ".smap"
}

internal fun defaultXmlReport() = "reports/kover/report.xml"
internal fun defaultHtmlReport() = "reports/kover/html"

internal fun defaultXmlModuleReport() = "reports/kover/module-xml/report.xml"
internal fun defaultHtmlModuleReport() = "reports/kover/module-html"
