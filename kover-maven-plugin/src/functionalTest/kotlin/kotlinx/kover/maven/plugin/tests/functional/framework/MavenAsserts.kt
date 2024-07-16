/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.tests.functional.framework

import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.BINARY_REPORT_PATH
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.DEFAULT_HTML_REPORT_PATH
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.DEFAULT_IC_REPORT_PATH
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.DEFAULT_XML_REPORT_PATH
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.HTML_TASK_NAME
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.IC_TASK_NAME
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.INSTRUMENTATION_TASK_NAME
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.LOG_TASK_NAME
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.VERIFY_TASK_NAME
import kotlinx.kover.maven.plugin.tests.functional.framework.BuildConstants.XML_TASK_NAME
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

fun CheckerContext.assertBuildIsSuccessful(shouldBeSuccessful: Boolean = true) {
    if (shouldBeSuccessful) {
        assertTrue(isSuccessful, "Build must be successful")
    } else {
        assertFalse(isSuccessful, "Build must failed")
    }
}

fun CheckerContext.assertAllSkipped() {
    assertBinaryReportExists(false)
    assertDefaultXmlReportExists(false)
    assertDefaultHtmlReportExists(false)
    assertDefaultIcReportExists(false)

    assertKoverLogIs(INSTRUMENTATION_TASK_NAME, "Kover is disabled by property 'kover.skip'")
    assertKoverLogIs(XML_TASK_NAME, "Kover is disabled by property 'kover.skip'")
    assertKoverLogIs(LOG_TASK_NAME, "Kover is disabled by property 'kover.skip'")
    assertKoverLogIs(HTML_TASK_NAME, "Kover is disabled by property 'kover.skip'")
    assertKoverLogIs(VERIFY_TASK_NAME, "Kover is disabled by property 'kover.skip'")
    assertKoverLogIs(IC_TASK_NAME, "Kover is disabled by property 'kover.skip'")
}

fun CheckerContext.assertVerificationPassed() {
    assertKoverLogIs(VERIFY_TASK_NAME, "Coverage rule checks passed successfully")
}


fun CheckerContext.assertNoVerificationRules() {
    assertKoverLogIs(VERIFY_TASK_NAME, "No Kover verification rules")
}

fun CheckerContext.assertBinaryReportExists(shouldExists: Boolean = true) {
    val report = findFile(BINARY_REPORT_PATH)
    if (shouldExists) {
        assertTrue(report.exists(), "Binary report does not exist, path $BINARY_REPORT_PATH")
        assertTrue(report.length() > 0, "Binary report is empty, path $BINARY_REPORT_PATH")
    } else {
        assertFalse(report.exists(), "Binary report exist but expected to be missed, path $BINARY_REPORT_PATH")
    }
}

fun CheckerContext.assertDefaultXmlReportExists(shouldExists: Boolean = true, modulePath: String? = null) {
    assertXmlReportExists(DEFAULT_XML_REPORT_PATH, shouldExists, modulePath)
}
fun CheckerContext.assertXmlReportExists(path: String, shouldExists: Boolean = true, modulePath: String? = null) {
    val report = findFile(path, modulePath)
    if (shouldExists) {
        assertTrue(report.exists(), "XML report does not exist, path $path")
    } else {
        assertFalse(report.exists(), "XML report exist but expected to be missed, path $path")
    }
}

fun CheckerContext.checkDefaultXmlReport(modulePath: String? = null, assertions: XmlReportContent.() -> Unit) {
    checkXmlReport(DEFAULT_XML_REPORT_PATH, modulePath, assertions)
}

fun CheckerContext.checkXmlReport(path: String, modulePath: String? = null, assertions: XmlReportContent.() -> Unit) {
    assertXmlReportExists(path, modulePath = modulePath)
    val xmlReport = parseXmlReport(findFile(path, modulePath))
    assertions(xmlReport)
}

fun CheckerContext.assertDefaultIcReportExists(shouldExists: Boolean = true) {
    assertIcReportExists(DEFAULT_IC_REPORT_PATH, shouldExists)
}

fun CheckerContext.assertIcReportExists(path: String, shouldExists: Boolean = true) {
    val report = findFile(path)
    if (shouldExists) {
        assertTrue(report.exists(), "IC report does not exist, path $path")
        assertTrue(report.length() > 0, "IC report is empty, path $path")
    } else {
        assertFalse(report.exists(), "IC report exist but expected to be missed, path $path")
    }
}

fun CheckerContext.assertDefaultHtmlReportExists(shouldExists: Boolean = true) {
    assertHtmlReportExists(DEFAULT_HTML_REPORT_PATH, shouldExists)
}

fun CheckerContext.assertHtmlReportExists(path: String, shouldExists: Boolean = true) {
    val report = findFile(path)
    val exists = report.resolve("index.html").exists()
    if (shouldExists) {
        assertTrue(exists, "HTML report does not exist, path $path")
    } else {
        assertFalse(exists, "HTML report exist but expected to be missed, path $path")
    }
}

fun CheckerContext.assertKoverLogIs(taskName: String, text: String) {
    val taskLog = koverGoalLog(taskName)
    assertTrue(taskLog.contains(text), "Task '$taskName' log differs, expected:\n$text\nactual:\n$taskLog")
}

fun CheckerContext.assertLogContains(vararg text: String) {
    text.forEach { searchedText ->
        assertTrue(searchedText in log, "Substring '$searchedText' is expected to be present in the logs")
    }
}

fun CheckerContext.assertDefaultHtmlTitle(title: String) {
    assertHtmlTitle(DEFAULT_HTML_REPORT_PATH, title)
}

fun CheckerContext.assertHtmlTitle(path: String, title: String) {
    val report = findFile(path)
    val indexPage = report.resolve("index.html")

    val actual = indexPage.readText().substringAfter("Current scope: ").substringBefore("<span")
    assertEquals(title, actual, "Incorrect title in HTML, $path")
}

fun CheckerContext.assertDefaultXmlTitle(title: String) {
    assertXmlTitle(DEFAULT_XML_REPORT_PATH, title)
}

fun CheckerContext.assertXmlTitle(path: String, title: String) {
    val report = findFile(path)

    // <report name="foo">
    val actual = report.readText().substringAfter("<report name=\"").substringBefore("\"")
    assertEquals(title, actual, "Incorrect title in XML, $path")
}

