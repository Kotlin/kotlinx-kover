package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.cases.utils.*
import kotlinx.kover.test.functional.core.*
import kotlinx.kover.test.functional.core.BaseGradleScriptTest
import kotlin.test.*

internal class TasksOrderingTests : BaseGradleScriptTest() {
    @Test
    fun testProjectTasks() {
        val build = diverseBuild()
        build.addKoverRootProject {
            sourcesFrom("simple")
            kover {
                verify {
                    rule {
                        bound {
                            minValue = 100
                        }
                    }
                }
            }
        }
        val runner = build.prepare()
        runner.runWithError("koverVerify", "koverReport") {
            // reports should be generated even if verification failed with an error
            checkDefaultReports()
        }
    }

    @Test
    fun testMergedTasks() {
        val build = diverseBuild()
        build.addKoverRootProject {
            sourcesFrom("simple")
            koverMerged {
                enable()
                verify {
                    rule {
                        bound {
                            minValue = 100
                        }
                    }
                }
            }
        }
        val runner = build.prepare()
        runner.runWithError("koverMergedVerify", "koverMergedReport") {
            // reports should be generated even if verification failed with an error
            checkDefaultMergedReports()
        }
    }

}
