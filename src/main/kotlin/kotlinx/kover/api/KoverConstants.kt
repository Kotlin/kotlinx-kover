/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

public object KoverNames {
    public const val CHECK_TASK_NAME = "check"
    public const val VERIFICATION_GROUP = "verification"

    public const val PROJECT_EXTENSION_NAME = "kover"
    public const val MERGED_EXTENSION_NAME = "koverMerged"
    public const val TASK_EXTENSION_NAME = "kover"

    public const val MERGED_XML_REPORT_TASK_NAME = "koverMergedXmlReport"
    public const val MERGED_HTML_REPORT_TASK_NAME = "koverMergedHtmlReport"
    public const val MERGED_REPORT_TASK_NAME = "koverMergedReport"
    public const val MERGED_VERIFY_TASK_NAME = "koverMergedVerify"

    public const val XML_REPORT_TASK_NAME = "koverXmlReport"
    public const val HTML_REPORT_TASK_NAME = "koverHtmlReport"
    public const val REPORT_TASK_NAME = "koverReport"
    public const val VERIFY_TASK_NAME = "koverVerify"

    public const val CONFIGURATION_NAME = "KoverConfig"
}

public object KoverPaths {
    public const val MERGED_HTML_REPORT_DEFAULT_PATH = "reports/kover/merged/html"
    public const val MERGED_XML_REPORT_DEFAULT_PATH = "reports/kover/merged/xml/report.xml"
    public const val MERGED_VERIFICATION_REPORT_DEFAULT_PATH = "reports/kover/merged/verification/errors.txt"

    public const val PROJECT_HTML_REPORT_DEFAULT_PATH = "reports/kover/html"
    public const val PROJECT_XML_REPORT_DEFAULT_PATH = "reports/kover/xml/report.xml"
    public const val PROJECT_VERIFICATION_REPORT_DEFAULT_PATH = "reports/kover/verification/errors.txt"
}

public object KoverVersions {
    public const val MINIMUM_GRADLE_VERSION = "6.8"

    internal const val KOVER_TOOL_MINIMAL_VERSION = "1.0.683"
    internal const val KOVER_TOOL_DEFAULT_VERSION = "1.0.683"
    internal const val JACOCO_TOOL_DEFAULT_VERSION = "0.8.8"

    // DEPRECATIONS
    // TODO delete deprecations in 0.8.x
    @Deprecated(
        message = "Constant was renamed to [KOVER_TOOL_MINIMAL_VERSION]. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("KOVER_TOOL_MINIMAL_VERSION"),
        level = DeprecationLevel.ERROR
    )
    internal const val MINIMAL_INTELLIJ_VERSION = KOVER_TOOL_MINIMAL_VERSION
    @Deprecated(
        message = "Constant was renamed to [KOVER_TOOL_DEFAULT_VERSION]. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("KOVER_TOOL_DEFAULT_VERSION"),
        level = DeprecationLevel.ERROR
    )
    internal const val DEFAULT_INTELLIJ_VERSION = KOVER_TOOL_DEFAULT_VERSION
    @Deprecated(
        message = "Constant was renamed to [JACOCO_TOOL_DEFAULT_VERSION]. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        replaceWith = ReplaceWith("JACOCO_TOOL_DEFAULT_VERSION"),
        level = DeprecationLevel.ERROR
    )
    internal const val DEFAULT_JACOCO_VERSION = JACOCO_TOOL_DEFAULT_VERSION
}

public object KoverMigrations {
    public const val MIGRATION_0_5_TO_0_6 = "https://github.com/Kotlin/kotlinx-kover/blob/v0.6.0/docs/migration-to-0.6.0.md"
    public const val MIGRATION_0_6_TO_0_7 = "https://github.com/Kotlin/kotlinx-kover/blob/v0.7.0-Beta/docs/migration-to-0.7.0.md"
}
