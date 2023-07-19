package kotlinx.kover.gradle.plugin.dsl

import kotlinx.kover.gradle.plugin.commons.htmlReportTaskName
import kotlinx.kover.gradle.plugin.commons.binaryReportTaskName
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
     * Name of the configuration to add dependency on Kover reports from another project.
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
     * Name of the binary report generation task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public const val DEFAULT_BINARY_REPORT_NAME = "koverBinaryReport"

    /**
     * Name of the HTML report generation task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public const val DEFAULT_HTML_REPORT_NAME = "koverHtmlReport"

    /**
     * Name of the verification task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public const val DEFAULT_VERIFY_REPORT_NAME = "koverVerify"

    /**
     * Name of the coverage logging task for Kotlin JVM and Kotlin multiplatform projects.
     */
    public const val DEFAULT_LOG_REPORT_NAME = "koverLog"

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
     * Name of the binary report generation task for [buildVariant] Android build variant for Android projects.
     */
    public fun androidBinaryReport(buildVariant: String): String {
        return binaryReportTaskName(buildVariant)
    }

    /**
     * Name of the verification task for [buildVariant] Android build variant for Android projects.
     */
    public fun androidVerify(buildVariant: String): String {
        return verifyTaskName(buildVariant)
    }

    /**
     * Name of the coverage logging task for [buildVariant] Android build variant for Android projects.
     */
    public fun androidLog(buildVariant: String): String {
        return logTaskName(buildVariant)
    }
}

/**
 * Name of the XML report generation task for Kotlin JVM and Kotlin multiplatform projects.
 *
 * Has the same value as [KoverNames.DEFAULT_XML_REPORT_NAME].
 */
public val TaskContainer.koverXmlReportName
    get() = KoverNames.DEFAULT_XML_REPORT_NAME

/**
 * Name of the HTML report generation task for Kotlin JVM and Kotlin multiplatform projects.
 *
 * Has the same value as [KoverNames.DEFAULT_HTML_REPORT_NAME].
 */
public val TaskContainer.koverHtmlReportName
    get() = KoverNames.DEFAULT_HTML_REPORT_NAME

/**
 * Name of the binary report generation task for Kotlin JVM and Kotlin multiplatform projects.
 *
 * Has the same value as [KoverNames.DEFAULT_BINARY_REPORT_NAME].
 */
public val TaskContainer.koverBinaryReportName
    get() = KoverNames.DEFAULT_BINARY_REPORT_NAME

/**
 * Name of the verification task for Kotlin JVM and Kotlin multiplatform projects.
 *
 * Has the same value as [KoverNames.DEFAULT_VERIFY_REPORT_NAME].
 */
public val TaskContainer.koverVerifyName
    get() = KoverNames.DEFAULT_VERIFY_REPORT_NAME

/**
 * Name of the coverage logging task for Kotlin JVM and Kotlin multiplatform projects.
 *
 * Has the same value as [KoverNames.DEFAULT_LOG_REPORT_NAME].
 */
public val TaskContainer.koverLogName
    get() = KoverNames.DEFAULT_LOG_REPORT_NAME


/**
 * Name of the XML report generation task for [buildVariantName] Android build variant for Android projects.
 *
 * Returns the same value as [KoverNames.androidXmlReport].
 */
public fun TaskContainer.koverAndroidXmlReportName(buildVariantName: String): String {
    return KoverNames.androidXmlReport(buildVariantName)
}

/**
 * Name of the HTML report generation task for [buildVariantName] Android build variant for Android projects.
 *
 * Returns the same value as [KoverNames.androidHtmlReport].
 */
public fun TaskContainer.koverAndroidHtmlReportName(buildVariantName: String): String {
    return KoverNames.androidHtmlReport(buildVariantName)
}

/**
 * Name of the binary report generation task for [buildVariantName] Android build variant for Android projects.
 *
 * Returns the same value as [KoverNames.androidBinaryReport].
 */
public fun TaskContainer.koverAndroidBinaryReportName(buildVariantName: String): String {
    return KoverNames.androidBinaryReport(buildVariantName)
}

/**
 * Name of the XML report generation task for [buildVariantName] Android build variant for Android projects.
 *
 * Returns the same value as [KoverNames.androidVerify].
 */
public fun TaskContainer.koverAndroidVerifyName(buildVariantName: String): String {
    return KoverNames.androidVerify(buildVariantName)
}

/**
 * Name of the coverage logging task for [buildVariantName] Android build variant for Android projects.
 *
 * Returns the same value as [KoverNames.androidLog].
 */
public fun TaskContainer.koverAndroidLogName(buildVariantName: String): String {
    return KoverNames.androidLog(buildVariantName)
}

/**
 * Name of the project extension to configure Kover measurements.
 */
public val ExtensionContainer.koverExtensionName: String
    get() = KoverNames.PROJECT_EXTENSION_NAME

/**
 * Name of the project extension to configure Kover reports.
 */
public val ExtensionContainer.koverReportExtensionName: String
    get() = KoverNames.REPORT_EXTENSION_NAME
