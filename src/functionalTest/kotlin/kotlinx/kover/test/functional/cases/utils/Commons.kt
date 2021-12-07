package kotlinx.kover.test.functional.cases.utils

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.RunResult
import kotlin.test.*

internal fun RunResult.checkDefaultBinaryReport(mustExists: Boolean = true) {
    val binary: String = defaultBinaryReport(engine, projectType)

    if (mustExists) {
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

        if (mustExists) {
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

internal fun RunResult.checkDefaultReports(mustExists: Boolean = true) {
    checkReports(defaultXmlReport(), defaultHtmlReport(), mustExists)
}

internal fun RunResult.checkDefaultModuleReports(mustExists: Boolean = true) {
    checkReports(defaultXmlModuleReport(), defaultHtmlModuleReport(), mustExists)
}

internal fun RunResult.checkReports(xmlPath: String, htmlPath: String, mustExists: Boolean) {
    if (mustExists) {
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


