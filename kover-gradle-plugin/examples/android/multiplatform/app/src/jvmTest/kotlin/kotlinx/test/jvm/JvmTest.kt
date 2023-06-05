package kotlinx.kover.test.jvm

import kotlin.test.Test

object JvmTest {

    @Test
    fun test() {
        JvmClass.foo()
        println("JVM")
    }
}