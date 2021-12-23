package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.assertCounterFullyCovered
import kotlinx.kover.test.functional.cases.utils.defaultXmlProjectReport
import kotlinx.kover.test.functional.cases.utils.defaultXmlReport
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlin.test.*

internal class AdaptersTests : BaseGradleScriptTest() {
    @Test
    fun testSubprojectHasAnotherPlugin() {
        /*
            Tests for https://github.com/Kotlin/kotlinx-kover/issues/100
            Classes from plugins applied in subproject not accessible for Kover in root project.
            Therefore, Kover is forced to use reflection to work with extensions of the kotlin multiplatform plugin.
         */
        internalSample("different-plugins")
            .run("koverXmlReport") {
                xml(defaultXmlReport()) {
                    assertCounterFullyCovered(classCounter("org.jetbrains.CommonClass"))
                    assertCounterFullyCovered(classCounter("org.jetbrains.JvmClass"))
                }
            }

        internalSample("different-plugins")
            .run("koverXmlProjectReport") {
                subproject("subproject-multiplatform") {
                    xml(defaultXmlProjectReport()) {
                        assertCounterFullyCovered(classCounter("org.jetbrains.CommonClass"))
                        assertCounterFullyCovered(classCounter("org.jetbrains.JvmClass"))
                    }
                }
            }
    }
}
