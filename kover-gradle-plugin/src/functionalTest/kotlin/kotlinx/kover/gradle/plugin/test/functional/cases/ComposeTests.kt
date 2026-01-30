/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.CheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.TemplateTest

internal class ComposeTests {
    @TemplateTest("android-compose", [":app:koverXmlReportDebug", ":app:koverHtmlReportDebug"])
    fun CheckerContext.testCompose() {
        subproject("app") {
            xmlReport("debug") {
                methodCounter("org.jetbrains.composetests.ComposeFunctionsKt", "Simple").assertFullyCovered()
                methodCounter("org.jetbrains.composetests.ComposeFunctionsKt", "Simple", "BRANCH").assertFullyCovered()

                methodCounter("org.jetbrains.composetests.ComposeFunctionsKt", "WithParam").assertFullyCovered()
                methodCounter("org.jetbrains.composetests.ComposeFunctionsKt", "WithParam", "BRANCH").assertFullyCovered()

                methodCounter("org.jetbrains.composetests.ComposeFunctionsKt", "WithDefParam").assertFullyCovered()
                methodCounter("org.jetbrains.composetests.ComposeFunctionsKt", "WithDefParam", "BRANCH").assertFullyCovered()
            }
        }
    }

    @TemplateTest("android-compose-8", [":app:koverXmlReportDebug", ":app:koverHtmlReportDebug"])
    fun CheckerContext.testComposeBefore9() {
        subproject("app") {
            xmlReport("debug") {
                methodCounter("org.jetbrains.composetests.ComposeFunctionsKt", "Simple").assertFullyCovered()
                methodCounter("org.jetbrains.composetests.ComposeFunctionsKt", "Simple", "BRANCH").assertFullyCovered()

                methodCounter("org.jetbrains.composetests.ComposeFunctionsKt", "WithParam").assertFullyCovered()
                methodCounter("org.jetbrains.composetests.ComposeFunctionsKt", "WithParam", "BRANCH").assertFullyCovered()

                methodCounter("org.jetbrains.composetests.ComposeFunctionsKt", "WithDefParam").assertFullyCovered()
                methodCounter("org.jetbrains.composetests.ComposeFunctionsKt", "WithDefParam", "BRANCH").assertFullyCovered()
            }
        }
    }
}