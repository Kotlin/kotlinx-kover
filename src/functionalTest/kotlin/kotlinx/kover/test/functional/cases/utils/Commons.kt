package kotlinx.kover.test.functional.cases.utils

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.RunResult
import kotlin.test.*

internal fun RunResult.checkIntellijBinaryReport(binary: String, smap: String, mustExists: Boolean = true) {
    if (mustExists) {
        file(binary) {
            assertTrue { exists() }
            assertTrue { length() > 0 }
        }
        file(smap) {
            assertTrue { exists() }
            assertTrue { length() > 0 }
        }
    } else {
        file(binary) {
            assertFalse { exists() }
        }
        file(smap) {
            assertFalse { exists() }
        }
    }
}

internal fun RunResult.checkJacocoBinaryReport(binary: String, mustExists: Boolean = true) {
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
}

internal fun RunResult.checkReports(xmlPath: String, htmlPath: String, mustExists: Boolean = true) {
    if (mustExists) {
        file(xmlPath) {
            assertTrue { exists() }
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

internal fun assertCounterExcluded(counter: Counter?, engine: CoverageEngine) {
    if (engine == CoverageEngine.INTELLIJ) {
        assertNull(counter)
    } else {
        assertNotNull(counter)
        assertEquals(0, counter.covered)
    }
}

internal fun assertCounterCoveredAndIncluded(counter: Counter?) {
    assertNotNull(counter)
    assertTrue { counter.covered > 0 }
}
