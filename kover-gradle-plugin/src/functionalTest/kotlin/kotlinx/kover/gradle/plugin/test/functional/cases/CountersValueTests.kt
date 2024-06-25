/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

internal class CountersValueTests {

    @TemplateTest("counters", ["koverXmlReport"])
    fun CheckerContext.testBasicCounterCases() {
        xmlReport {
            // test on branch counter
            methodCounter("org.jetbrains.MyBranchedClass", "foo", type = "BRANCH").assertCovered(1, 3)

            // test on constructor of used sealed classes
            methodCounter("org.jetbrains.Sealed", "<init>").assertFullyCovered()
            methodCounter("org.jetbrains.SealedWithInit", "<init>").assertFullyCovered()
            methodCounter("org.jetbrains.SealedWithConstructor", "<init>").assertFullyCovered()

            // test on empty objects
            classCounter("org.jetbrains.UnusedObject").assertFullyMissed()
            classCounter("org.jetbrains.UsedObject").assertFullyCovered()

            // deprecated functions (ERROR and HIDDEN) should not be included in the report.
            methodCounter("org.jetbrains.Different", "deprecatedError").assertAbsent()
            methodCounter("org.jetbrains.Different", "deprecatedHidden").assertAbsent()
            // WARNING deprecated functions should be included in the report.
            methodCounter("org.jetbrains.Different", "deprecatedWarn").assertFullyMissed()

            // empty functions must be included in the report with line counter
            methodCounter("org.jetbrains.Different", "emptyFun", type = "LINE").assertFullyMissed()

            // Instruction counters - value may depend on Kotlin compiler version
            methodCounter("org.jetbrains.Different", "helloWorld").assertTotal(4)

            // Test on try with resources in Java
            methodCounter("org.jetbrains.TryWithResources", "test", "LINE").assertFullyCovered()
            methodCounter("org.jetbrains.TryWithResources", "test", "BRANCH").assertFullyCovered()

            methodCounter("org.jetbrains.TryFinally", "testWithCatch", "LINE").assertCovered(2, 1)
        }
    }

    @TemplateTest("k2-counters", ["koverXmlReport"])
    fun CheckerContext.testK2() {
        xmlReport {
            // test enums
            classCounter("org.jetbrains.K2Enum", type = "LINE").assertFullyCovered()
        }
    }

}
