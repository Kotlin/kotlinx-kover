package org.jetbrains.serialuser

import org.jetbrains.CommonClass
import org.jetbrains.CommonInternalClass
import kotlin.test.Test

class TestClass {
    @Test
    fun callCommonTest() {
        CommonClass().callFromThisProject()
    }

    @Test
    fun callInternalTest() {
        CommonInternalClass().function()
    }
}
