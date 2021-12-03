package kotlinx.kover.test.functional.cases

import kotlinx.kover.api.*
import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlin.test.*

internal class InstrumentationFilteringTests : BaseGradleScriptTest() {

    @Test
    fun testExcludeIntellij() {
        builder()
            .case("Test exclusion of classes from instrumentation for IntelliJ coverage agent")
            .languages(GradleScriptLanguage.KOTLIN, GradleScriptLanguage.GROOVY)
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .engines(CoverageEngine.INTELLIJ)
            .sources("simple")
            .configTest(
                """excludes = listOf("org.jetbrains.*Exa?ple*")""",
                """excludes = ['org.jetbrains.*Exa?ple*']"""
            )
            .build()
            .run("build") {
                xml(DEFAULT_XML) {
                    assertNull(classCounter("org.jetbrains.ExampleClass"))
                    assertNotNull(classCounter("org.jetbrains.UnusedClass"))
                }
            }
    }

    @Test
    fun testExcludeJaCoCo() {
        builder()
            .case("Test exclusion of classes from instrumentation for JaCoCo coverage agent")
            .languages(GradleScriptLanguage.KOTLIN, GradleScriptLanguage.GROOVY)
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .engines(CoverageEngine.JACOCO)
            .sources("simple")
            .configTest(
                """excludes = listOf("org.jetbrains.*Exa?ple*")""",
                """excludes = ['org.jetbrains.*Exa?ple*']"""
            )
            .build()
            .run("build") {
                xml(DEFAULT_XML) {
                    assertEquals(0, classCounter("org.jetbrains.ExampleClass")!!.covered)
                    assertNotNull(classCounter("org.jetbrains.UnusedClass"))
                }
            }
    }

}
