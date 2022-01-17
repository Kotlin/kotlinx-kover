package org.jetbrains.serialuser

import org.jetbrains.MyBranchedClass
import kotlin.test.Test

class TestClass {
    @Test
    fun testBranches() {
        MyBranchedClass().foo(-20)
    }
}
