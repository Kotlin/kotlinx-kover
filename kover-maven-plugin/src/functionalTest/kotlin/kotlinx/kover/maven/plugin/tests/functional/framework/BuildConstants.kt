/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.tests.functional.framework

object BuildConstants {
    const val BINARY_REPORT_PATH = "kover/test.ic"

    const val BUILD_DIRECTORY = "target"
    const val DEFAULT_REPORT_DIR = "site/kover"
    const val DEFAULT_XML_REPORT_PATH = "$DEFAULT_REPORT_DIR/report.xml"
    const val DEFAULT_IC_REPORT_PATH = "$DEFAULT_REPORT_DIR/report.ic"
    const val DEFAULT_HTML_REPORT_PATH = "$DEFAULT_REPORT_DIR/html"

    const val INSTRUMENTATION_TASK_NAME = "instrumentation"
    const val XML_TASK_NAME = "report-xml"
    const val LOG_TASK_NAME = "log"
    const val HTML_TASK_NAME = "report-html"
    const val VERIFY_TASK_NAME = "verify"
    const val IC_TASK_NAME = "report-ic"
    const val ARTIFACT_TASK_NAME = "print-artifact-info"
}