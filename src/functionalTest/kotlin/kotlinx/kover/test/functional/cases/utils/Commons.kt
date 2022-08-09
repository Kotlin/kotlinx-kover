/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.test.functional.cases.utils

import kotlinx.kover.test.functional.core.RunResult
import org.gradle.testkit.runner.*
import kotlin.test.*

internal fun RunResult.checkDefaultBinaryReport(mustExist: Boolean = true) {
    if (mustExist) {
        file(defaultBinaryReport) {
            assertTrue { exists() }
            assertTrue { length() > 0 }
        }
    } else {
        file(defaultBinaryReport) {
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
