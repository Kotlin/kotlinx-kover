package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.*
import kotlin.test.*

internal class DefaultConfigTests : BaseGradleScriptTest() {
    @Test
    fun testImplicitConfigsJvm() {
        runner()
            .case("Test default setting for Kotlin/JVM")
            .languages(GradleScriptLanguage.GROOVY, GradleScriptLanguage.KOTLIN)
            .types(ProjectType.KOTLIN_JVM)
            .sources("simple-single")
            .check("build") {
                checkIntellijBinaryReport(DEFAULT_INTELLIJ_KJVM_BINARY, DEFAULT_INTELLIJ_KJVM_SMAP)
                checkReports(DEFAULT_XML, DEFAULT_HTML)
            }
    }

    @Test
    fun testImplicitConfigsKmp() {
        runner()
            .case("Test default setting for Kotlin Multi-Platform")
            .languages(GradleScriptLanguage.GROOVY, GradleScriptLanguage.KOTLIN)
            .types(ProjectType.KOTLIN_MULTIPLATFORM)
            .sources("simple-single")
            .check("build") {
                checkIntellijBinaryReport(DEFAULT_INTELLIJ_KMP_BINARY, DEFAULT_INTELLIJ_KMP_SMAP)
                checkReports(DEFAULT_XML, DEFAULT_HTML)
            }
    }

}
