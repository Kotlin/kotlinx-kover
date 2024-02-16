package org.jetbrains.serialuser

import org.jetbrains.foo.ExampleClass
import foo.bar.FooClass
import kotlin.test.Test

class TestClass {
    @Test
    fun simpleTest() {
        ExampleClass().used(-20)
        FooClass().function()
    }

}
