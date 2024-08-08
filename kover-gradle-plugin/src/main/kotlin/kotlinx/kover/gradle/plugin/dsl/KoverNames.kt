/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.KoverMigrations
import kotlinx.kover.gradle.plugin.commons.binaryReportTaskName
import kotlinx.kover.gradle.plugin.commons.htmlReportTaskName
import kotlinx.kover.gradle.plugin.commons.logTaskName
import kotlinx.kover.gradle.plugin.commons.verifyTaskName
import kotlinx.kover.gradle.plugin.commons.xmlReportTaskName
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.TaskContainer

/**
 * An object with public names of Kover objects that can be accessed by this name.
 */
public object KoverNames {
    /**
     * ID of Kover Gradle Plugin.
     */
    public val pluginId: String
        get() = KOVER_PLUGIN_ID

    /**
     * Name of reports variant for JVM targets.
     * It includes all code from a project using the Kotlin/JVM plugin, or the code of the JVM target from a project using Kotlin/Multiplatform.
     */
    public val jvmVariantName: String
        get() = JVM_VARIANT_NAME

    /**
     * Name of the configuration to add dependency on Kover reports from another project.
     */
    public val configurationName: String
        get() = KOVER_DEPENDENCY_NAME

    /**
     * Name of the project extension to configure Kover.
     */
    public val extensionName: String
        get() = KOVER_PROJECT_EXTENSION_NAME

    /**
     * Name of the XML report generation task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public val koverXmlReportName
        get() = XML_REPORT_NAME

    /**
     * Name of the HTML report generation task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public val koverHtmlReportName
        get() = HTML_REPORT_NAME

    /**
     * Name of the binary report generation task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public val koverBinaryReportName
        get() = BINARY_REPORT_NAME

    /**
     * Name of the verification task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public val koverVerifyName
        get() = VERIFY_REPORT_NAME

    /**
     * Name of the coverage logging task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public val koverLogName
        get() = LOG_REPORT_NAME


    /**
     * Name of the XML report generation task for [variant] Kover report variant.
     */
    public fun koverXmlReportName(variant: String): String {
        return xmlReportTaskName(variant)
    }

    /**
     * Name of the HTML report generation task for [variant] Kover report variant.
     */
    public fun koverHtmlReportName(variant: String): String {
        return htmlReportTaskName(variant)
    }

    /**
     * Name of the binary report generation task for [variant] Kover report variant.
     */
    public fun koverBinaryReportName(variant: String): String {
        return binaryReportTaskName(variant)
    }

    /**
     * Name of the verification task for [variant] Kover report variant.
     */
    public fun koverVerifyName(variant: String): String {
        return verifyTaskName(variant)
    }

    /**
     * Name of the coverage logging task for [variant] Kover report variant.
     */
    public fun koverLogName(variant: String): String {
        return logTaskName(variant)
    }
}
