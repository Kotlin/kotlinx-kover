package org.jetbrains.serialuser

import org.jetbrains.*
import kotlin.test.Test

class TestClass {
    @Test
    fun testBranches() {
        MyBranchedClass().foo(-20)
    }

    @Test
    fun testSealed() {
        SealedChild(1)
        SealedWithInitChild(2)
        SealedWithConstructorChild(3)
    }

    @Test
    fun testObjects() {
        UsedObject.toString()
        UsedObjectWithVal.toString()
        UsedObjectWithInit.toString()
        UsedObjectWithFun.toString()
        UsedObjectFun.hello()
    }
}
