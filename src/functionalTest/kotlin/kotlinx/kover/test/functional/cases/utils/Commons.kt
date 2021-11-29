package kotlinx.kover.test.functional.cases.utils

import kotlinx.kover.test.functional.core.RunResult
import kotlin.test.*

internal fun RunResult.checkIntellijBinaryReport(binary: String, smap: String, mustExists: Boolean = true) {
    if (mustExists) {
        assertTrue { file(binary).exists() }
        assertTrue { file(binary).length() > 0 }
        assertTrue { file(smap).exists() }
        assertTrue { file(smap).length() > 0 }
    } else {
        assertFalse { file(binary).exists() }
        assertFalse { file(smap).exists() }
    }
}

internal fun RunResult.checkJacocoBinaryReport(binary: String, mustExists: Boolean = true) {
    if (mustExists) {
        assertTrue { file(binary).exists() }
        assertTrue { file(binary).length() > 0 }
    } else {
        assertFalse { file(binary).exists() }
    }
}

internal fun RunResult.checkReports(xmlPath: String, htmlPath: String, mustExists: Boolean = true) {
    if (mustExists) {
        assertTrue { file(xmlPath).exists() }
        assertTrue { file(xmlPath).length() > 0 }
        assertTrue { file(htmlPath).exists() }
        assertTrue { file(htmlPath).isDirectory }
    } else {
        assertFalse { file(xmlPath).exists() }
        assertFalse { file(htmlPath).exists() }
    }

}
