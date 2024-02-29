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

    // ===
    // Deprecations
    // Remove in 0.9.0

    @Deprecated(
        message = "Kover renaming: Symbol PLUGIN_ID was removed, use pluginId instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("pluginId"),
        level = DeprecationLevel.ERROR
    )
    public const val PLUGIN_ID = KOVER_PLUGIN_ID

    @Deprecated(
        message = "Kover renaming: Symbol DEPENDENCY_CONFIGURATION_NAME was removed, use configurationName instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("configurationName"),
        level = DeprecationLevel.ERROR
    )
    public const val DEPENDENCY_CONFIGURATION_NAME = KOVER_DEPENDENCY_NAME

    @Deprecated(
        message = "Kover renaming: Symbol PROJECT_EXTENSION_NAME was removed, use extensionName instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("extensionName"),
        level = DeprecationLevel.ERROR
    )
    public const val PROJECT_EXTENSION_NAME = KOVER_PROJECT_EXTENSION_NAME

    @Deprecated(
        message = "Kover renaming: Symbol REPORT_EXTENSION_NAME was removed, use extensionName instead Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("extensionName"),
        level = DeprecationLevel.ERROR
    )
    public const val REPORT_EXTENSION_NAME = "koverReport"

    @Deprecated(
        message = "Kover renaming: Symbol DEFAULT_XML_REPORT_NAME was removed, use koverXmlReportName instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("koverXmlReportName"),
        level = DeprecationLevel.ERROR
    )
    public const val DEFAULT_XML_REPORT_NAME = "koverXmlReport"

    @Deprecated(
        message = "Kover renaming: Symbol DEFAULT_BINARY_REPORT_NAME was removed, use koverBinaryReportName instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("koverBinaryReportName"),
        level = DeprecationLevel.ERROR
    )
    public const val DEFAULT_BINARY_REPORT_NAME = "koverBinaryReport"

    @Deprecated(
        message = "Kover renaming: Symbol DEFAULT_HTML_REPORT_NAME was removed, use koverHtmlReport instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("koverHtmlReportName"),
        level = DeprecationLevel.ERROR
    )
    public const val DEFAULT_HTML_REPORT_NAME = "koverHtmlReport"

    @Deprecated(
        message = "Kover renaming: Symbol DEFAULT_VERIFY_REPORT_NAME was removed, use koverVerifyName instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("koverVerifyName"),
        level = DeprecationLevel.ERROR
    )
    public const val DEFAULT_VERIFY_REPORT_NAME = "koverVerify"

    @Deprecated(
        message = "Kover renaming: Symbol DEFAULT_LOG_REPORT_NAME was removed, use koverLogName instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
        replaceWith = ReplaceWith("koverLogName"),
        level = DeprecationLevel.ERROR
    )
    public const val DEFAULT_LOG_REPORT_NAME = "koverLog"
}


// ===
// Deprecations
// Remove in 0.9.0

@Deprecated(
    message = "Property koverXmlReportName was removed, use KoverNames.koverXmlReportName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
    level = DeprecationLevel.ERROR
)
public val TaskContainer.koverXmlReportName: String
    get() {
        throw KoverDeprecationException("Property koverXmlReportName was removed, use KoverNames.koverXmlReportName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

@Deprecated(
    message = "Property koverHtmlReportName was removed, use KoverNames.koverHtmlReportName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
    level = DeprecationLevel.ERROR
)
public val TaskContainer.koverHtmlReportName: String
    get() {
        throw KoverDeprecationException("Property koverHtmlReportName was removed, use KoverNames.koverHtmlReportName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

@Deprecated(
    message = "Property koverBinaryReportName was removed, use KoverNames.koverBinaryReportName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
    level = DeprecationLevel.ERROR
)
public val TaskContainer.koverBinaryReportName: String
    get() {
        throw KoverDeprecationException("Property koverBinaryReportName was removed, use KoverNames.koverBinaryReportName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

@Deprecated(
    message = "Property koverVerifyName was removed, use KoverNames.koverVerifyName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
    level = DeprecationLevel.ERROR
)
public val TaskContainer.koverVerifyName: String
    get() {
        throw KoverDeprecationException("Property koverVerifyName was removed, use KoverNames.koverVerifyName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

@Deprecated(
    message = "Property koverLogName was removed, use KoverNames.koverLogName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
    level = DeprecationLevel.ERROR
)
public val TaskContainer.koverLogName: String
    get() {
        throw KoverDeprecationException("Property koverLogName was removed, use KoverNames.koverLogName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

@Deprecated(
    message = "Function koverAndroidXmlReportName was removed, use KoverNames.koverXmlReportName(buildVariantName) function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
    level = DeprecationLevel.ERROR
)
@Suppress("UNUSED_PARAMETER")
public fun TaskContainer.koverAndroidXmlReportName(buildVariantName: String): String {
    throw KoverDeprecationException("Function koverAndroidXmlReportName was removed, use KoverNames.koverXmlReportName(buildVariantName) function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
}

@Deprecated(
    message = "Function koverAndroidHtmlReportName was removed, use KoverNames.koverHtmlReportName(buildVariantName) function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
    level = DeprecationLevel.ERROR
)
@Suppress("UNUSED_PARAMETER")
public fun TaskContainer.koverAndroidHtmlReportName(buildVariantName: String): String {
    throw KoverDeprecationException("Function koverAndroidHtmlReportName was removed, use KoverNames.koverHtmlReportName(buildVariantName) function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
}

@Deprecated(
    message = "Function koverAndroidBinaryReportName was removed, use KoverNames.koverBinaryReportName function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
    level = DeprecationLevel.ERROR
)
@Suppress("UNUSED_PARAMETER")
public fun TaskContainer.koverAndroidBinaryReportName(buildVariantName: String): String {
    throw KoverDeprecationException("Function koverAndroidBinaryReportName was removed, use koverBinaryReportName function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
}

@Deprecated(
    message = "Function koverAndroidVerifyName was removed, use KoverNames.koverVerifyName function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
    level = DeprecationLevel.ERROR
)
@Suppress("UNUSED_PARAMETER")
public fun TaskContainer.koverAndroidVerifyName(buildVariantName: String): String {
    throw KoverDeprecationException("Function koverAndroidVerifyName was removed, use KoverNames.koverVerifyName function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
}

@Deprecated(
    message = "Function koverAndroidLogName was removed, use KoverNames.koverLogName(buildVariantName) function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
    level = DeprecationLevel.ERROR
)
@Suppress("UNUSED_PARAMETER")
public fun TaskContainer.koverAndroidLogName(buildVariantName: String): String {
    throw KoverDeprecationException("Function koverAndroidLogName was removed, use KoverNames.koverLogName(buildVariantName) function. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
}

@Deprecated(
    message = "Property koverExtensionName was removed, use KoverNames.extensionName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
    level = DeprecationLevel.ERROR
)
public val ExtensionContainer.koverExtensionName: String
    get() {
        throw KoverDeprecationException("Property koverExtensionName was removed, use KoverNames.extensionName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }

@Deprecated(
    message = "Property koverReportExtensionName was removed, use KoverNames.extensionName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}",
    level = DeprecationLevel.ERROR
)
public val ExtensionContainer.koverReportExtensionName: String
    get() {
        throw KoverDeprecationException("Property koverReportExtensionName was removed, use KoverNames.extensionName property. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_7_TO_0_8}")
    }
