package kotlinx.kover.test.functional.cases.utils

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.RunResult
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

    if (engine == CoverageEngine.INTELLIJ) {
        val smap = defaultSmapFile(projectType)

        if (mustExist) {
            file(smap) {
                assertTrue { exists() }
                assertTrue { length() > 0 }
            }
        } else {
            file(smap) {
                assertFalse { exists() }
            }
        }
    }
}

internal fun RunResult.checkDefaultReports(mustExist: Boolean = true) {
    checkReports(defaultXmlReport(), defaultHtmlReport(), mustExist)
}

internal fun RunResult.checkDefaultProjectReports(mustExist: Boolean = true) {
    checkReports(defaultXmlProjectReport(), defaultHtmlProjectReport(), mustExist)
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

internal fun assertCounterAbsent(counter: Counter?) {
    assertNull(counter)
}

internal fun assertCounterExcluded(counter: Counter?, engine: CoverageEngine) {
    if (engine == CoverageEngine.INTELLIJ) {
        assertNull(counter)
    } else {
        assertNotNull(counter)
        assertEquals(0, counter.covered)
    }
}

internal fun assertCounterCovered(counter: Counter?) {
    assertNotNull(counter)
    assertTrue { counter.covered > 0 }
}

internal fun assertCounterFullyCovered(counter: Counter?) {
    assertNotNull(counter)
    assertTrue { counter.covered > 0 }
    assertEquals(0, counter.missed)
}


