/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.ExamplesTest

internal class ExamplesBuildTests {
    @ExamplesTest("jvm/defaults")
    fun CheckerContext.jvmDefaultValues() {
        // build only
    }

    @ExamplesTest("jvm/merged")
    fun CheckerContext.jvmMerged() {
        // build only
    }

    @ExamplesTest("jvm/minimal")
    fun CheckerContext.jvmMinimal() {
        // build only
    }

    @ExamplesTest("android/minimal_groovy")
    fun CheckerContext.androidGroovy() {
        // build only
    }

    @ExamplesTest("android/minimal_kts")
    fun CheckerContext.androidKts() {
        // build only
    }

    @ExamplesTest("android/multiproject")
    fun CheckerContext.androidMultiProject() {
        // build only
    }
}