package org.jetbrains.serialuser

import org.jetbrains.CommonClass
import org.jetbrains.UserClass
import kotlin.test.Test

class TestClass {
    @Test
    fun callCommonTest() {
        CommonClass().callFromAnotherProject()
    }

    @Test
    fun callUserTest() {
        UserClass().function()
    }
}
