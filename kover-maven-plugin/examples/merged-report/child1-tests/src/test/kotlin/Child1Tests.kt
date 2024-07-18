/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin.testing

import kotlin.test.Test

class Child1Tests {
    @Test
    fun test() {
        TestUtils().function()
        Child1Class().function()
    }
}