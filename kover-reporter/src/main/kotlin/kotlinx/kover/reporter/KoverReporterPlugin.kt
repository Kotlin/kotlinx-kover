/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.reporter

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.kover.reporter.report.JvmCoverageReport
import kotlinx.kover.reporter.report.Reporter
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaCompilation

class KoverReporterPlugin : Plugin<Project> {

    /**
     * Apply plugin to a given project.
     */
    override fun apply(target: Project) {
        target.pluginManager.withPlugin("kotlin") {
            target.createTask()
        }

        target.pluginManager.withPlugin("kotlin-multiplatform") {
            target.createTask()
        }
    }

    private fun Project.createTask() {
        tasks.register("printCoverage") {
            doLast {
                val extension = this@createTask.extensions.findByName("kotlin") as KotlinJvmProjectExtension
                val compilation = extension.target.compilations.getByName("main")

                this@createTask.print(compilation)
            }
        }
    }

    private fun Project.print(compilation: KotlinWithJavaCompilation<*, *>) {
        val classpath = compilation.output.allOutputs.files.toList()
        val sourceDirs = compilation.kotlinSourceSets.flatMap { it.kotlin.srcDirs }.toList()

        val report = Reporter().report(classpath, sourceDirs, rootDir)

        val format = Yaml(configuration = YamlConfiguration(encodeDefaults = false))
        val yaml = format.encodeToString(JvmCoverageReport.serializer(), report)
        logger.quiet("Structure:\n\n$yaml")
    }

}
