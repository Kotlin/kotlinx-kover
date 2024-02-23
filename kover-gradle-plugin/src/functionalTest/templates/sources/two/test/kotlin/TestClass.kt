package org.jetbrains.two.test

import org.jetbrains.two.TwoClass
import kotlin.test.Test

class TestClass {
    @Test
    fun test() {
        TwoClass().use(-20)
    }

}
