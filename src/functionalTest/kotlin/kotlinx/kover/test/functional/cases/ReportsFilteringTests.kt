package kotlinx.kover.test.functional.cases

import kotlinx.kover.test.functional.framework.checker.*
import kotlinx.kover.test.functional.framework.configurator.*
import kotlinx.kover.test.functional.framework.starter.*

internal class ReportsFilteringTests {

    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun BuildConfigurator.testExclude() {
        addKoverProject {
            sourcesFrom("simple")

            kover {
                filters {
                    classes {
                        excludes += "org.jetbrains.*Exa?ple*"
                    }
                }
            }
        }
        run("koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.ExampleClass").assertAbsent()
                classCounter("org.jetbrains.SecondClass").assertCovered()
            }
        }
    }

    @SlicedGeneratedTest(allLanguages = true, allTools = true)
    fun BuildConfigurator.testExcludeInclude() {
        addKoverProject {
            sourcesFrom("simple")

            kover {
                filters {
                    classes {
                        excludes += "org.jetbrains.*Exa?ple*"
                        includes += "org.jetbrains.*Cla?s"
                    }
                }
            }
        }
        run("koverXmlReport") {
            xml(defaultXmlReport()) {
                classCounter("org.jetbrains.ExampleClass").assertAbsent()
                classCounter("org.jetbrains.Unused").assertAbsent()
                classCounter("org.jetbrains.SecondClass").assertFullyCovered()
            }
        }
    }

}
