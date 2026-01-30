/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.composetests

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ComposableTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun basicTestCase() {
        rule.setContent {
            Simple()
            WithParam("test")
            WithDefParam()
            WithDefParam("test")
        }
    }
}