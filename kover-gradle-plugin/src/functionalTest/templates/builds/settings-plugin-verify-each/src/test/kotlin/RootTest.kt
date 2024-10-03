/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package tests.settings.root

import kotlin.test.Test
import tests.settings.subproject.UsedInRootClass

class RootTest {
    @Test
    fun test() {
        RootClass().action()

        UsedInRootClass().action()
    }
}