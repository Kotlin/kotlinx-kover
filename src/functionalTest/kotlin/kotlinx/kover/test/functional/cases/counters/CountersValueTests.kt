package kotlinx.kover.test.functional.cases.counters

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlin.test.*

internal class CountersValueTests : BaseGradleScriptTest() {
    @Test
    fun testBasicCounterCases() {
        val build = diverseBuild(
            engines = listOf(CoverageEngineVendor.INTELLIJ)
        )
        build.addKoverRootProject {
            sourcesFrom("counters")
        }

        val runner = build.prepare()
        runner.run("koverXmlReport") {
            xml(defaultXmlReport()) {
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
            }
        }
    }
}
