package kotlinx.kover.examples.merged

import kotlinx.kover.examples.merged.subproject.*
import kotlin.test.Test

class TestClasses {
    @Test
    fun testThisProject() {
        ExampleClass().formatInt(50)
    }

    @Test
    fun testExcludedProject() {
        ClassFromSecondProject().foo()
    }

    @Test
    fun testSubproject() {
        SubprojectFirstClass().formatInt(42)
        SubprojectSecondClass().formatDouble(4.2)
    }
}
