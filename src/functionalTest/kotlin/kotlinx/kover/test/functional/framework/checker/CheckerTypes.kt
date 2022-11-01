/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.framework.checker

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.framework.common.*
import java.io.*

internal interface CheckerContext {
    val definedKoverVersion: String?

    val toolVariant: CoverageToolVariant

    val language: ScriptLanguage

    val output: String

    val buildScript: String

    /**
     * Perform built-in checks.
     */
    fun prepare(buildErrorExpected: Boolean)

    fun allProjects(checker: CheckerContext.() -> Unit)

    /**
     * `null` if no Kotlin plugin has not been applied
     */
    val pluginType: KotlinPluginType?

    fun subproject(path: String, checker: CheckerContext.() -> Unit)

    fun output(checker: String.() -> Unit)

    fun file(name: String, checker: File.() -> Unit)

    fun xml(filename: String, checker: XmlReportChecker.() -> Unit)

    fun verification(checker: VerifyReportChecker.() -> Unit)

    val defaultBinaryReport: String
    fun checkReports(xmlPath: String, htmlPath: String, mustExist: Boolean)
    fun checkOutcome(taskNameOrPath: String, expectedOutcome: String)
    fun checkDefaultReports(mustExist: Boolean = true)
    fun checkDefaultMergedReports(mustExist: Boolean = true)
    fun checkDefaultBinaryReport(mustExist: Boolean = true)
}



internal interface Counter {
    fun assertAbsent()

    fun assertFullyMissed()

    fun assertCovered()

    fun assertTotal(expectedTotal: Int)

    fun assertCovered(covered: Int, missed: Int)

    fun assertFullyCovered()
}

internal interface VerifyReportChecker {
    fun assertKoverResult(expected: String)
    fun assertJaCoCoResult(expected: String)
}

internal interface XmlReportChecker {
    fun classCounter(className: String, type: String = "INSTRUCTION"): Counter
    fun methodCounter(className: String, methodName: String, type: String = "INSTRUCTION"): Counter
}
