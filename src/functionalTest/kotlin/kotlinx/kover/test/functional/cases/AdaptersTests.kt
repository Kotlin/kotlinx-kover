package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.assertCounterFullyCovered
import kotlinx.kover.test.functional.cases.utils.defaultXmlModuleReport
import kotlinx.kover.test.functional.cases.utils.defaultXmlReport
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlin.test.*

internal class AdaptersTests : BaseGradleScriptTest() {
    @Test
    fun testSubmoduleHasAnotherPlugin() {
        /*
            Tests for https://github.com/Kotlin/kotlinx-kover/issues/100
            Classes from plugins applied in submodule not accessible for Kover in root module.
            Therefore, Kover is forced to use reflection to work with extensions of the kotlin multiplatform plugin.
         */
        internalProject("different-plugins")
            .run("koverXmlReport") {
                xml(defaultXmlReport()) {
                    assertCounterFullyCovered(classCounter("org.jetbrains.CommonClass"))
                    assertCounterFullyCovered(classCounter("org.jetbrains.JvmClass"))
                }
            }

        internalProject("different-plugins")
            .run("koverXmlModuleReport") {
                submodule("submodule-multiplatform") {
                    xml(defaultXmlModuleReport()) {
                        assertCounterFullyCovered(classCounter("org.jetbrains.CommonClass"))
                        assertCounterFullyCovered(classCounter("org.jetbrains.JvmClass"))
                    }
                }
            }
    }
}
