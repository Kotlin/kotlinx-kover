/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

object KoverNames {
    /**
     * Name of the configurations to add dependency on Kover setup in another project.
     */
    public const val DEPENDENCY_CONFIGURATION_NAME = "kover"

    /**
     * Name of the project extension to configure Kover setup.
     */
    public const val PROJECT_EXTENSION_NAME = "kover"

    /**
     * Name of the project extension to configure Kover reports for Kotlin JVM and Kotlin Multiplatform projects.
     */
    public const val REGULAR_REPORT_EXTENSION_NAME = "koverReport"

    /**
     * Name of the project extension to configure Kover reports for Kotlin Android projects.
     */
    public const val ANDROID_EXTENSION_NAME = "koverAndroid"

    /**
     * Name of the XML report generation task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public const val REGULAR_XML_REPORT_NAME = "koverXmlReport"

    /**
     * Name of the HTML report generation task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public const val REGULAR_HTML_REPORT_NAME = "koverHtmlReport"

    /**
     * Name of the verification task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public const val REGULAR_VERIFY_REPORT_NAME = "koverVerify"
}
