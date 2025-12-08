/*
 * Copyright 2017-2025 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest

internal class AndroidKmpLibTests {
    @TemplateTest("android-kmp-library-8", ["koverXmlReport"])
    fun CheckerContext.testPresenceAgp8() {
       xmlReport {
           classCounter("org.jetbrains.ExampleClass").assertPresent()
           classCounter("org.jetbrains.AndroidClass").assertPresent()
           methodCounter("org.jetbrains.AndroidClass", "covered").assertFullyCovered()
       }
    }

    @TemplateTest("android-kmp-library", ["koverXmlReport"])
    fun CheckerContext.testPresence() {
       xmlReport {
           classCounter("org.jetbrains.ExampleClass").assertPresent()
           classCounter("org.jetbrains.AndroidClass").assertPresent()
           methodCounter("org.jetbrains.AndroidClass", "covered").assertFullyCovered()
       }
    }
}