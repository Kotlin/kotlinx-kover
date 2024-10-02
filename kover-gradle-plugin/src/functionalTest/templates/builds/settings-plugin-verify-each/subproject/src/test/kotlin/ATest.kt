/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package tests.settings.subproject

import kotlin.test.Test
import tests.settings.subproject2.ToRegularSubprojectClass

class SubprojectTest {
    @Test
    fun test() {
        SubprojectClass().action()
        ToRegularSubprojectClass().action()
    }
}