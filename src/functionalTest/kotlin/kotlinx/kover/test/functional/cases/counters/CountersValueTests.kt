package kotlinx.kover.test.functional.cases.counters

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlin.test.*

internal class CountersValueTests : BaseGradleScriptTest() {
    @Test
    fun testBasicCounterCases() {
        builder("Testing of basic counting capabilities by an IntelliJ Agent")
            .engines(CoverageEngine.INTELLIJ)
            .sources("counters")
            .build()
            .run("build") {
                xml(defaultMergedXmlReport()) {
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

                    // Instruction counters
                    // helloWorld contains 4 instructions. `return` ignored by reporter and the first instruction is not covered because of a bug in the IR compiler
                    // FIXME after https://youtrack.jetbrains.com/issue/KT-51080 is fixed, the number should become 3
                    methodCounter("org.jetbrains.Different", "helloWorld").assertTotal(2)
                }
            }
    }
}
