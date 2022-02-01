package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.assertFullyCovered
import kotlinx.kover.test.functional.cases.utils.defaultXmlReport
import kotlinx.kover.test.functional.cases.utils.defaultMergedXmlReport
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
            .run("koverMergedXmlReport") {
                xml(defaultMergedXmlReport()) {
                    classCounter("org.jetbrains.CommonClass").assertFullyCovered()
                    classCounter("org.jetbrains.JvmClass").assertFullyCovered()
                }
            }

        internalSample("different-plugins")
            .run("koverXmlReport") {
                subproject("subproject-multiplatform") {
                    xml(defaultXmlReport()) {
                        classCounter("org.jetbrains.CommonClass").assertFullyCovered()
                        classCounter("org.jetbrains.JvmClass").assertFullyCovered()
                    }
                }
            }
    }
}
