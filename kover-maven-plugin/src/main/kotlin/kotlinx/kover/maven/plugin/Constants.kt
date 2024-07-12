/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin

import org.apache.maven.artifact.Artifact

internal object Constants {
    /**
     * Default parameter name to pass to surefire plugin an argument for JVM in which the tests will be run.
     */
    const val AGENT_ARG_PARAMETER = "argLine"

    /**
     * Artifact group and ID for Kover JVM instrumentation agent.
     */
    const val AGENT_ARTIFACT = "org.jetbrains.kotlinx:kover-jvm-agent"

    /**
     * Relative path to store Kover JVM Agent arguments.
     */
    const val AGENT_ARGUMENTS_PATH = "kover/test.agent.args"

    /**
     * Relative path to store binary report for test run.
     */
    const val BIN_REPORT_PATH = "kover/test.ic"

    const val KOVER_REPORTS_PATH = "kover"

    /**
     * Default IC report path.
     */
    const val KOVER_IC_REPORT_NAME = "$KOVER_REPORTS_PATH/report.ic"

    /**
     * Default XML report path.
     */
    const val XML_REPORT_NAME = "$KOVER_REPORTS_PATH/report.xml"

    /**
     * Default HTML report path.
     */
    const val HTML_REPORT_DIR_NAME = "html"

    /**
     * Build directory for temporary files.
     */
    const val TMP_DIR_NAME = "tmp"

    /**
     * Scopes for dependencies from which the coverage for the aggregated report will be taken.
     */
    val DEPENDENCY_SCOPES =
        setOf(Artifact.SCOPE_COMPILE, Artifact.SCOPE_RUNTIME, Artifact.SCOPE_PROVIDED, Artifact.SCOPE_TEST)

    /**
     * Scope for dependencies from which only coverage info (not classes) will be taken.
     */
    const val TEST_SCOPE = Artifact.SCOPE_TEST
}