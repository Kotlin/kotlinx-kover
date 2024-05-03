/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest

internal class ConstantObjectTests {

    /**
     * All objects that contain only constants should be excluded from the report.
     */
    @TemplateTest("counters", ["koverXmlReport"])
    fun CheckerContext.testBasicCounterCases() {
        xmlReport {
            classCounter("org.jetbrains.ConstantHolder").assertAbsent()
            classCounter("org.jetbrains.InterfaceWithCompanion${"$"}Companion").assertAbsent()
            classCounter("org.jetbrains.InterfaceWithNamedCompanion${"$"}Named").assertAbsent()
        }
    }
}