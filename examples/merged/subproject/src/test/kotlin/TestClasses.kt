package kotlinx.kover.examples.merged

import kotlin.test.Test

class TestClasses {
    @Test
    fun testThisProject() {
        SubprojectFirstClass().printClass()
    }

}
