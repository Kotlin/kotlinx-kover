/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.tests.functional.framework

import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

interface CheckerContext {
    val isSuccessful: Boolean

    val log: String

    fun koverGoalLog(goalName: String, moduleId: String? = null): String

    fun findFile(relativePath: String, module: String? = null): File
}

class MavenAssertionException(message: String, cause: Throwable? = null): Exception(message, cause)

interface XmlReportContent {
    fun classCounter(className: String, type: CounterType): Counter
    fun methodCounter(className: String, methodName: String, type: CounterType): Counter
}

class Counter(
    val symbol: String,
    val type: CounterType,
    val values: CounterValues?
)

data class CounterValues(val missed: Int, val covered: Int)

enum class CounterType {
    INSTRUCTION,
    LINE,
    BRANCH,
    METHOD
}

sealed class CounterAssert {
    object IsAbsent : CounterAssert()
    object IsFullyMissed : CounterAssert()
    object IsFullyCovered : CounterAssert()
    object IsCovered : CounterAssert()
    class IsCoveredFor(val value: Int) : CounterAssert()
    class IsCoveredGreaterOrEquals(val value: Int) : CounterAssert()
    class Coverage(val covered: Int, val missed: Int) : CounterAssert()
}

infix fun Counter.assert(condition: CounterAssert) {
    when (condition) {
        CounterAssert.IsAbsent -> assertNull(values, "Counter '$symbol' with type '$type' isn't absent")
        CounterAssert.IsCovered -> {
            assertNotNull(values, "Counter '$symbol' with type '$type' isn't covered because it absent")
            assertTrue(values.covered > 0, "Counter '$symbol' with type '$type' isn't covered")
        }
        is CounterAssert.IsCoveredFor -> {
            assertNotNull(values, "Counter '$symbol' with type '$type' isn't covered because it absent")
            assertEquals(values.covered, condition.value, "Counter '$symbol' with type '$type' has illegal coverage")
        }
        is CounterAssert.IsCoveredGreaterOrEquals -> {
            assertNotNull(values, "Counter '$symbol' with type '$type' isn't covered because it absent")
            assertTrue(values.covered >= condition.value, "Counter '$symbol' with type '$type' expected to be covered more or equals ${condition.value} but actual ${values.covered}")
        }
        CounterAssert.IsFullyCovered -> {
            assertNotNull(values, "Counter '$symbol' with type '$type' is absent so fully covered can't be checked")

            // skip empty branches
            if (values.covered == 0 && values.missed == 0) return

            assertTrue(values.covered > 0, "Counter '$symbol' with type '$type' isn't fully covered")
            assertEquals(0, values.missed, "Counter '$symbol' with type '$type' isn't fully covered")
        }
        CounterAssert.IsFullyMissed -> {
            assertNotNull(values, "Counter '$symbol' with type '$type' isn't fully missed because it absent")
            assertTrue(values.missed > 0, "Counter '$symbol' with type '$type' isn't fully missed")
            assertEquals(0, values.covered, "Counter '$symbol' with type '$type' isn't fully missed")
        }
        is CounterAssert.Coverage -> {
            assertNotNull(values, "Counter '$symbol' with type '$type' isn't covered because it absent")
            assertEquals(values.covered, condition.covered, "Counter '$symbol' with type '$type' has illegal coverage")
            assertEquals(values.missed, condition.missed, "Counter '$symbol' with type '$type' has illegal missing")
        }
    }
}