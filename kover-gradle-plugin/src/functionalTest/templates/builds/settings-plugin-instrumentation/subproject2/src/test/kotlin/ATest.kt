/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package tests.settings.subproject2

import kotlin.test.Test

class Subproject2Test {
    @Test
    fun test() {
        Subproject2Class().action()
        Tested().action()
    }
}