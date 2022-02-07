package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlin.test.*

internal class ReportsFilteringTests : BaseGradleScriptTest() {

    @Test
    fun testExclude() {
        builder("Test exclusion of classes from XML report")
            .languages(GradleScriptLanguage.KOTLIN, GradleScriptLanguage.GROOVY)
            .sources("simple")
            .config(
                """
  tasks.koverMergedXmlReport {
    excludes = listOf("org.jetbrains.*Exa?ple*")
  }""".trimIndent(),
                """
  tasks.koverMergedXmlReport {
    excludes = ['org.jetbrains.*Exa?ple*']
  }""".trimIndent()
            )
            .build()
            .run("build") {
                xml(defaultMergedXmlReport()) {
                    classCounter("org.jetbrains.ExampleClass").assertAbsent()
                    classCounter("org.jetbrains.SecondClass").assertCovered()
                }
            }
    }

    @Test
    fun testExcludeInclude() {
        builder("Test inclusion and exclusion of classes in XML report")
            .languages(GradleScriptLanguage.KOTLIN, GradleScriptLanguage.GROOVY)
            .sources("simple")
            .config(
                """
  tasks.koverMergedXmlReport {
    includes = listOf("org.jetbrains.*Cla?s")
    excludes = listOf("org.jetbrains.*Exa?ple*")
  }""".trimIndent(),

                """
  tasks.koverMergedXmlReport {
    includes = ['org.jetbrains.*Cla?s']
    excludes = ['org.jetbrains.*Exa?ple*']
  }""".trimIndent()
            )
            .build()
            .run("build") {
                xml(defaultMergedXmlReport()) {
                    classCounter("org.jetbrains.ExampleClass").assertAbsent()
                    classCounter("org.jetbrains.Unused").assertAbsent()
                    classCounter("org.jetbrains.SecondClass").assertFullyCovered()
                }
            }
    }

}
