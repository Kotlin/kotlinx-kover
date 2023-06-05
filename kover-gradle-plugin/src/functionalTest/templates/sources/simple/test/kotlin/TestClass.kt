package org.jetbrains.serialuser

import org.jetbrains.ExampleClass
import org.jetbrains.SecondClass
import kotlin.test.Test

class TestClass {
    @Test
    fun simpleTest() {
        ExampleClass().used(-20)
    }

    @Test
    fun secondTest() {
        SecondClass().anotherUsed(-20)
    }
}
