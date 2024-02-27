/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.test.functional.framework.mirroring

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.kover.gradle.plugin.test.functional.framework.common.ScriptLanguage
import org.gradle.api.Project
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


private val groovyExample = """
            reports {
                total {
                    xml {
                        xmlFile = layout.buildDirectory.file("foo")
                        def var0 = layout.projectDirectory.file("bar")
                        xmlFile = var0
                        xmlFile = var0
                    }
                    filters {
                        excludes {
                            classes("AAA", "bbb")
                        }
                    }
                    verify {
                        rule("my Rule") {
                            bound(10, 20, kotlinx.kover.gradle.plugin.dsl.MetricType.LINE, kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE)
                        }
                    }
                }
            }
            
        """.trimIndent()

private val ktsExample = """
            reports {
                total {
                    xml {
                        xmlFile.set(layout.buildDirectory.file("foo"))
                        val var0 = layout.projectDirectory.file("bar")
                        xmlFile.set(var0)
                        xmlFile.set(var0)
                    }
                    filters {
                        excludes {
                            classes("AAA", "bbb")
                        }
                    }
                    verify {
                        rule("my Rule") {
                            bound(10, 20, kotlinx.kover.gradle.plugin.dsl.MetricType.LINE, kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE)
                        }
                    }
                }
            }
            
        """.trimIndent()

private val ktsAfter8Example = """
    reports {
        total {
            xml {
                xmlFile = layout.buildDirectory.file("foo")
                val var0 = layout.projectDirectory.file("bar")
                xmlFile = var0
                xmlFile = var0
            }
            filters {
                excludes {
                    classes("AAA", "bbb")
                }
            }
            verify {
                rule("my Rule") {
                    bound(10, 20, kotlinx.kover.gradle.plugin.dsl.MetricType.LINE, kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE)
                }
            }
        }
    }
    
""".trimIndent()

class MirroringTest {

    @Test
    fun test() {
        val groovyScript = printGradleDsl<KoverProjectExtension, Project>(ScriptLanguage.GROOVY, "7.1.0", block = config)
        assertEquals(groovyExample, groovyScript)

        val ktsScript = printGradleDsl<KoverProjectExtension, Project>(ScriptLanguage.KTS, "7.1.0", block = config)
        assertEquals(ktsExample, ktsScript)

        val ktsAfter8Script = printGradleDsl<KoverProjectExtension, Project>(ScriptLanguage.KTS, "8.1.0", block = config)
        assertEquals(ktsAfter8Example, ktsAfter8Script)
    }


    private val config: KoverProjectExtension.(Project) -> Unit = {

        reports {
            total {
                val my = it.layout
                xml {
                    xmlFile.set(it.layout.buildDirectory.file("foo"))
                    val file = my.projectDirectory.file("bar")
                    xmlFile.set(file)
                    xmlFile.set(file)
                }

                filters {
                    excludes {
                        classes("AAA", "bbb")
                    }
                }

                verify {
                    rule("my Rule") {
                        // default values are always printed
                        bound(10, 20)
                    }
                }
            }
        }

    }

}
