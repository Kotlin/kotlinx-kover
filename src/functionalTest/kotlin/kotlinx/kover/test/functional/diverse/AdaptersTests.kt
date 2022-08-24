package kotlinx.kover.test.functional.diverse

import kotlinx.kover.test.functional.diverse.core.defaultXmlReport
import kotlinx.kover.test.functional.diverse.core.defaultMergedXmlReport
import kotlinx.kover.test.functional.diverse.core.AbstractDiverseGradleTest
import kotlin.test.*

internal class AdaptersTests : AbstractDiverseGradleTest() {
    @Test
    fun testSubprojectHasAnotherPlugin() {
        /*
            Tests for https://github.com/Kotlin/kotlinx-kover/issues/100
            Classes from plugins applied in subproject not accessible for Kover in root project.
            Therefore, Kover is forced to use reflection to work with extensions of the kotlin multiplatform plugin.
         */
        sampleBuild("different-plugins")
            .run("koverMergedXmlReport") {
                xml(defaultMergedXmlReport()) {
                    classCounter("org.jetbrains.CommonClass").assertFullyCovered()
                    classCounter("org.jetbrains.JvmClass").assertFullyCovered()
                }
            }

        sampleBuild("different-plugins")
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
