/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import org.gradle.language.base.plugins.LifecycleBasePlugin

public object KoverNames {
    public const val CHECK_TASK_NAME = LifecycleBasePlugin.CHECK_TASK_NAME
    public const val VERIFICATION_GROUP = LifecycleBasePlugin.VERIFICATION_GROUP

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

    public const val CONFIGURATION_ENGINE_NAME = "KoverEngineConfig"
    public const val CONFIGURATION_AGGREGATE_NAME = "koverAggregate"
    public const val CONFIGURATION_SOURCE_FILES_PRODUCER_NAME = "koverSourcesElements"
    public const val CONFIGURATION_SOURCE_FILES_CONSUMER_NAME = "koverSources"
    public const val CONFIGURATION_BINARY_REPORT_PRODUCER_FILES_NAME = "koverBinaryReportElements"
    public const val CONFIGURATION_BINARY_REPORT_CONSUMER_FILES_NAME = "koverBinaryReports"
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
    internal const val MINIMAL_INTELLIJ_VERSION = "1.0.680"
    internal const val DEFAULT_INTELLIJ_VERSION = "1.0.680"

    internal const val DEFAULT_JACOCO_VERSION = "0.8.8"
}

public object KoverMigrations {
    public const val MIGRATION_0_5_TO_0_6 =
        "https://github.com/Kotlin/kotlinx-kover/blob/v0.6.0/docs/migration-to-0.6.0.md"
}
