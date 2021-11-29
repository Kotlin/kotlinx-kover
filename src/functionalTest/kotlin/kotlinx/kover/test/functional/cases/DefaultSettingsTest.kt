package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.core.*
import kotlin.test.*

internal class DefaultSettingsTest : BaseGradleScriptTest() {
    @Test
    fun testDefaultSettingsJvm() {
        runner()
            .case("Test default setting for Kotlin/JVM")
            .languages(GradleScriptLanguage.GROOVY, GradleScriptLanguage.KOTLIN)
            .types(ProjectType.KOTLIN_JVM)
            .sources("simple-single")
            .check("build") {
                checkIntellijBinaryReport("kover/test.ic")
                checkReportsSimple("reports/kover/report.xml", "reports/kover/html")
            }
    }

    @Test
    fun testDefaultSettingsKmp() {
        runner()
            .case("Test default setting for Kotlin Multi-Platform")
            .languages(GradleScriptLanguage.GROOVY, GradleScriptLanguage.KOTLIN)
            .types(ProjectType.KOTLIN_MULTIPLATFORM)
            .sources("simple-single")
            .check("build") {
                checkIntellijBinaryReport("kover/jvmTest.ic")
                checkReportsSimple("reports/kover/report.xml", "reports/kover/html")
            }
    }


    private fun RunResult.checkIntellijBinaryReport(path: String) {
        assertTrue { file(path).exists() }
        assertTrue { file(path).length() > 0 }
        assertTrue { file("$path.smap").exists() }
        assertTrue { file("$path.smap").length() > 0 }
    }

    private fun RunResult.checkReportsSimple(xmlPath: String, htmlPath: String) {
        assertTrue { file(xmlPath).exists() }
        assertTrue { file(xmlPath).length() > 0 }
        assertTrue { file(htmlPath).exists() }
        assertTrue { file(htmlPath).isDirectory }
    }
}
