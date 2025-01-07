/*
 * Copyright 2017-2025 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.cases

import kotlinx.kover.gradle.plugin.test.functional.framework.checker.createCheckerContext
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.buildFromTemplate
import kotlinx.kover.gradle.plugin.test.functional.framework.runner.runWithParams
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class SourceSetsTests {

    @Test
    fun testExclude() {
        val template = buildFromTemplate("sourcesets-multi")

        template.kover {
            currentProject {
                sources.excludedSourceSets.addAll("main", "foo")
            }
        }

        val gradleBuild = template.generate()
        val buildResult = gradleBuild.runWithParams(":koverXmlReport")

        gradleBuild.createCheckerContext(buildResult).xmlReport {
            assertTrue(buildResult.isSuccessful, "Build should be successful")
            classCounter("kotlinx.kover.examples.sourcesets.MainClass").assertAbsent()
            classCounter("kotlinx.kover.examples.sourcesets.FooClass").assertAbsent()
            classCounter("kotlinx.kover.examples.sourcesets.ExtraClass").assertPresent()
        }
    }

    @Test
    fun testInclude() {
        val template = buildFromTemplate("sourcesets-multi")

        template.kover {
            currentProject {
                sources.includedSourceSets.addAll("extra")
            }
        }

        val gradleBuild = template.generate()
        val buildResult = gradleBuild.runWithParams(":koverXmlReport")

        gradleBuild.createCheckerContext(buildResult).xmlReport {
            assertTrue(buildResult.isSuccessful, "Build should be successful")
            classCounter("kotlinx.kover.examples.sourcesets.MainClass").assertAbsent()
            classCounter("kotlinx.kover.examples.sourcesets.FooClass").assertAbsent()
            classCounter("kotlinx.kover.examples.sourcesets.ExtraClass").assertPresent()
        }
    }

    @Test
    fun testInclude2() {
        val template = buildFromTemplate("sourcesets-multi")

        template.kover {
            currentProject {
                sources.includedSourceSets.addAll("extra", "foo")
            }
        }

        val gradleBuild = template.generate()
        val buildResult = gradleBuild.runWithParams(":koverXmlReport")

        gradleBuild.createCheckerContext(buildResult).xmlReport {
            assertTrue(buildResult.isSuccessful, "Build should be successful")
            classCounter("kotlinx.kover.examples.sourcesets.MainClass").assertAbsent()
            classCounter("kotlinx.kover.examples.sourcesets.FooClass").assertPresent()
            classCounter("kotlinx.kover.examples.sourcesets.ExtraClass").assertPresent()
        }
    }

    @Test
    fun testMixed() {
        val template = buildFromTemplate("sourcesets-multi")

        template.kover {
            currentProject {
                sources {
                    includedSourceSets.addAll("extra", "foo")
                    excludedSourceSets.add("foo")
                }
            }
        }

        val gradleBuild = template.generate()
        val buildResult = gradleBuild.runWithParams(":koverXmlReport")

        gradleBuild.createCheckerContext(buildResult).xmlReport {
            assertTrue(buildResult.isSuccessful, "Build should be successful")
            classCounter("kotlinx.kover.examples.sourcesets.MainClass").assertAbsent()
            classCounter("kotlinx.kover.examples.sourcesets.FooClass").assertAbsent()
            classCounter("kotlinx.kover.examples.sourcesets.ExtraClass").assertPresent()
        }
    }

    @Test
    fun testTestSourceSet() {
        val template = buildFromTemplate("sourcesets-multi")

        template.kover {
            currentProject {
                sources {
                    includedSourceSets.addAll("test")
                }
            }
        }

        val gradleBuild = template.generate()
        val buildResult = gradleBuild.runWithParams(":koverXmlReport")

        gradleBuild.createCheckerContext(buildResult).xmlReport {
            assertTrue(buildResult.isSuccessful, "Build should be successful")
            classCounter("kotlinx.kover.examples.sourcesets.MainClass").assertAbsent()
            classCounter("kotlinx.kover.examples.sourcesets.FooClass").assertAbsent()
            classCounter("kotlinx.kover.examples.sourcesets.ExtraClass").assertAbsent()
            classCounter("kotlinx.kover.examples.sourcesets.TestClasses").assertPresent()
        }
    }


}