/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.ExamplesTest

internal class ExamplesBuildTests {

    @ExamplesTest("jvm/merged")
    fun CheckerContext.jvmMerged() {
        // build only
    }

    @ExamplesTest("jvm/single")
    fun CheckerContext.jvmSingle() {
        // build only
    }

    @ExamplesTest("jvm/single-kmp")
    fun CheckerContext.jvmSingleKmp() {
        // build only
    }

    @ExamplesTest("jvm/copy-variant", ["koverXmlReportFirst", "koverXmlReportSecond"])
    fun CheckerContext.jvmCopyVariant() {
        xmlReport("first") {
            // only classes from `first` project are present
            classCounter("kotlinx.kover.examples.merged.SubprojectFirstClass").assertCovered()
            classCounter("kotlinx.kover.examples.merged.ClassFromSecondProject").assertAbsent()
        }

        xmlReport("second") {
            // only classes from `second` project are present
            classCounter("kotlinx.kover.examples.merged.SubprojectFirstClass").assertAbsent()
            classCounter("kotlinx.kover.examples.merged.ClassFromSecondProject").assertCovered()
        }
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

    @ExamplesTest("android/multiproject-custom", [":koverHtmlReportCustom"])
    fun CheckerContext.androidMultiProjectWithCustomVariant() {
        // build only
    }

    @ExamplesTest("android/with-jvm", [":koverHtmlReportCustom"])
    fun CheckerContext.androidWithJvm() {
        // build only
    }
}