package kotlinx.kover.test.android

import kotlinx.kover.test.android.lib.MagicFactory
import org.junit.Test

import org.junit.Assert.*


class LocalLibTests {
    @Test
    fun testDebugUtils() {
        assertEquals(42, MagicFactory.generate())
    }
}