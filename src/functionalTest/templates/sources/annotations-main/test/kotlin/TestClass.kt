package org.jetbrains.serialuser

import org.jetbrains.*
import kotlin.test.Test

class TestClass {

    @Test
    fun test() {
        NotExcludedClass().function()
        ExcludedClass().function()

        val partially = PartiallyExcludedClass()
        partially.function1()
        partially.function2()
        partially.inlined()

        inlinedExcluded()
        // don't call `inlinedNotExcluded` and `notExcluded` - they must be executed transitive through `inlinedExcluded`

        val lambda = createLambda()
        lambda()
    }

}
