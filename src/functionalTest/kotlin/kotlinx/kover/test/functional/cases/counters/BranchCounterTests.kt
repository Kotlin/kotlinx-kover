package kotlinx.kover.test.functional.cases.counters

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlin.test.*

internal class BranchCounterTests: BaseGradleScriptTest() {
    @Test
    fun testBranchCounter() {
        builder("Test simple branch counters")
            .engines(CoverageEngine.INTELLIJ, CoverageEngine.JACOCO)
            .sources("branches")
            .build()
            .run("build") {
                xml(defaultXmlReport()) {
                    val counter = methodCounter("org.jetbrains.MyBranchedClass", "foo", type = "BRANCH")
                    assertNotNull(counter)
                    assertEquals(1, counter.covered)
                    assertEquals(3, counter.missed)
                }
            }
    }
}
