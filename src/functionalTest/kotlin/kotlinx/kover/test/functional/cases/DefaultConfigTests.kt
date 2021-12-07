package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.*
import kotlin.test.*

internal class DefaultConfigTests : BaseGradleScriptTest() {
    @Test
    fun testImplicitConfigs() {
        builder("Test implicit default settings")
            .languages(GradleScriptLanguage.GROOVY, GradleScriptLanguage.KOTLIN)
            .types(ProjectType.KOTLIN_JVM, ProjectType.KOTLIN_MULTIPLATFORM)
            .sources("simple")
            .build()
            .run("build") {
                checkDefaultBinaryReport()
                checkDefaultReports()
            }
    }

}
