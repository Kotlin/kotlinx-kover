package kotlinx.kover.test.functional.cases.utils

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.RunResult
import org.gradle.testkit.runner.*
import kotlin.test.*

internal fun RunResult.checkDefaultBinaryReport(mustExist: Boolean = true) {
    val binary: String = defaultBinaryReport(engine, projectType)

    if (mustExist) {
        file(binary) {
            assertTrue { exists() }
            assertTrue { length() > 0 }
        }
    } else {
        file(binary) {
            assertFalse { exists() }
        }
    }
}

internal fun RunResult.checkDefaultMergedReports(mustExist: Boolean = true) {
    checkReports(defaultMergedXmlReport(), defaultMergedHtmlReport(), mustExist)
}

internal fun RunResult.checkDefaultReports(mustExist: Boolean = true) {
    checkReports(defaultXmlReport(), defaultHtmlReport(), mustExist)
}

internal fun RunResult.checkOutcome(taskName: String, outcome: TaskOutcome) {
    outcome(taskName) {
        assertEquals(outcome, this)
    }
}

internal fun RunResult.checkReports(xmlPath: String, htmlPath: String, mustExist: Boolean) {
    if (mustExist) {
        file(xmlPath) {
            assertTrue("XML file must exist '$xmlPath'") { exists() }
            assertTrue { length() > 0 }
        }
        file(htmlPath) {
            assertTrue { exists() }
            assertTrue { isDirectory }
        }
    } else {
        file(xmlPath) {
            assertFalse { exists() }
        }
        file(htmlPath) {
            assertFalse { exists() }
        }
    }
}

internal fun RunResult.checkIntellijErrors(errorExpected: Boolean = false) {
    if (engine != CoverageEngine.INTELLIJ) return

    file(errorsDirectory()) {
        if (this.exists() && !errorExpected) {
            val errorLogs = this.listFiles()?.map { it.name } ?: emptyList()
            throw AssertionError("Detected IntelliJ Coverage Engine errors: $errorLogs")
        }
    }
}

internal fun Counter?.assertAbsent() {
    assertNull(this)
}

internal fun Counter?.assertFullyMissed() {
    assertNotNull(this)
    assertTrue { this.missed > 0 }
    assertEquals(0, this.covered)
}

internal fun Counter?.assertCovered() {
    assertNotNull(this)
    assertTrue { this.covered > 0 }
}


internal fun Counter?.assertTotal(count: Int) {
    assertNotNull(this)
    assertEquals(count, covered + missed)
}

internal fun Counter?.assertCovered(covered: Int, missed: Int) {
    assertNotNull(this)
    assertEquals(covered, this.covered)
    assertEquals(missed, this.missed)
}

internal fun Counter?.assertFullyCovered() {
    assertNotNull(this)
    assertTrue { this.covered > 0 }
    assertEquals(0, this.missed)
}


