/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.commons.htmlReportTaskName
import kotlinx.kover.gradle.plugin.commons.verifyTaskName
import kotlinx.kover.gradle.plugin.commons.xmlReportTaskName

object KoverNames {
    /**
     * Name of the configurations to add dependency on Kover reports from another project.
     */
    public const val DEPENDENCY_CONFIGURATION_NAME = "kover"

    /**
     * Name of the project extension to configure Kover measurements.
     */
    public const val PROJECT_EXTENSION_NAME = "kover"

    /**
     * Name of the project extension to configure Kover reports.
     */
    public const val REPORT_EXTENSION_NAME = "koverReport"

    /**
     * Name of the XML report generation task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public const val DEFAULT_XML_REPORT_NAME = "koverXmlReport"

    /**
     * Name of the HTML report generation task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public const val DEFAULT_HTML_REPORT_NAME = "koverHtmlReport"

    /**
     * Name of the verification task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public const val DEFAULT_VERIFY_REPORT_NAME = "koverVerify"

    /**
     * Name of the XML report generation task for [buildVariant] Android build variant for Android projects.
     */
    public fun androidXmlReport(buildVariant: String): String {
        return xmlReportTaskName(buildVariant)
    }

    /**
     * Name of the HTML report generation task for [buildVariant] Android build variant for Android projects.
     */
    public fun androidHtmlReport(buildVariant: String): String {
        return htmlReportTaskName(buildVariant)
    }

    /**
     * Name of the verification task for [buildVariant] Android build variant for Android projects.
     */
    public fun androidVerifyReport(buildVariant: String): String {
        return verifyTaskName(buildVariant)
    }
}
