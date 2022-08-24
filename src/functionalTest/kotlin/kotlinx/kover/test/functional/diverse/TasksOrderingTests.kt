package kotlinx.kover.test.functional.diverse

import kotlinx.kover.test.functional.diverse.core.*
import kotlinx.kover.test.functional.diverse.core.AbstractDiverseGradleTest
import kotlin.test.*

internal class TasksOrderingTests : AbstractDiverseGradleTest() {
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
