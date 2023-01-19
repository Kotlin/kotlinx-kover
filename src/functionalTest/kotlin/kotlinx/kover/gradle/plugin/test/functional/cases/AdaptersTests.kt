package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.*
import kotlinx.kover.gradle.plugin.test.functional.framework.starter.*

/**
Tests for https://github.com/Kotlin/kotlinx-kover/issues/100
Classes from plugins applied in subproject not accessible for Kover in root project.
Therefore, Kover is forced to use reflection to work with extensions of the kotlin multiplatform plugin.
 */
internal class AdaptersTests {
    @TemplateTest("different-plugins", ["koverXmlReport"])
    fun CheckerContext.testMerged() {
        xml(defaultXmlReport()) {
            classCounter("org.jetbrains.CommonClass").assertFullyCovered()
            classCounter("org.jetbrains.JvmClass").assertFullyCovered()
        }
    }

    @TemplateTest("different-plugins", ["koverXmlReport"])
    fun CheckerContext.testSubprojectHasAnotherPlugin() {
        subproject(":subproject-multiplatform") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.CommonClass").assertFullyCovered()
                classCounter("org.jetbrains.JvmClass").assertFullyCovered()
            }
        }
    }
}
