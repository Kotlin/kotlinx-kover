package org.jetbrains.serialuser

import org.jetbrains.CommonClass
import org.jetbrains.UserClass
import kotlin.test.Test

class TestClass {
    @Test
    fun callCommonTest() {
        CommonClass().callFromAnotherModule()
    }

    @Test
    fun callUserTest() {
        UserClass().function()
    }
}
