package kotlinx.kover.test.android

import kotlinx.kover.test.android.Maths
import org.junit.Test

import org.junit.Assert.*


class LocalTests {
    @Test
    fun testDebugUtils() {
        assertEquals(3, Maths.sum(1, 2))
    }
}