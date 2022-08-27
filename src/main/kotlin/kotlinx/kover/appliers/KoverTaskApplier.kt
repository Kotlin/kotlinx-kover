/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.appliers

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.AgentFilters
import kotlinx.kover.engines.commons.EngineManager
import kotlinx.kover.tasks.EngineDetails
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.create
import org.gradle.process.CommandLineArgumentProvider
import java.io.File


internal fun Test.applyToTestTask(
    projectExtension: KoverProjectConfig,
    engineProvider: Provider<EngineDetails>
) {
    val extension = createTaskExtension(projectExtension)

    val disabledProvider = project.provider {
        (projectExtension.isDisabled.get() || extension.isDisabled.get() ||
                projectExtension.instrumentation.excludeTasks.contains(name))
    }

    val filtersProvider = project.filtersProvider(projectExtension, extension)

    jvmArgumentProviders.add(
        CoverageArgumentProvider(
            this,
            filtersProvider,
            engineProvider,
            disabledProvider,
            extension.reportFile
        )
    )

    val sourceErrorProvider = project.provider {
        File(extension.reportFile.get().asFile.parentFile, "coverage-error.log")
    }
    val targetErrorProvider = project.layout.buildDirectory.file("kover/errors/$name.log").map { it.asFile }

    doFirst(BinaryReportCleanupAction(disabledProvider, extension.reportFile, engineProvider.map { e -> e.variant }))
    doLast(MoveIntellijErrorLogAction(sourceErrorProvider, targetErrorProvider))
}

private fun Task.createTaskExtension(projectExtension: KoverProjectConfig): KoverTaskExtension {
    val taskExtension = extensions.create<KoverTaskExtension>(KoverNames.TASK_EXTENSION_NAME, project.objects)

    taskExtension.isDisabled.convention(false)

    val reportFile = project.layout.buildDirectory.zip(projectExtension.engine) { buildDir, engine ->
        val suffix = if (engine.vendor == CoverageEngineVendor.INTELLIJ) ".ic" else ".exec"
        buildDir.file("kover/$name$suffix")
    }

    taskExtension.reportFile.convention(reportFile)

    return taskExtension
}


private class CoverageArgumentProvider(
    private val task: Task,
    @get:Nested val filtersProvider: Provider<AgentFilters>,
    @get:Nested val engineProvider: Provider<EngineDetails>,
    @get:Input val disabledProvider: Provider<Boolean>,
    @get:OutputFile val reportFileProvider: Provider<RegularFile>
) : CommandLineArgumentProvider, Named {
    @Internal
    override fun getName(): String {
        return "koverArgumentsProvider"
    }

    override fun asArguments(): MutableIterable<String> {
        if (disabledProvider.get()) {
            return mutableListOf()
        }

        val reportFile = reportFileProvider.get().asFile
        var filters = filtersProvider.get()

        /*
            The instrumentation of android classes often causes errors when using third-party
            frameworks (see https://github.com/Kotlin/kotlinx-kover/issues/89).

            Because android classes are not part of the project, in any case they do not get into the report,
            and they can be excluded from instrumentation.

            FIXME Remove this code if the IntelliJ Agent stops changing project classes during instrumentation, see https://github.com/Kotlin/kotlinx-kover/issues/196
        */
        filters = filters.appendExcludedTo("android.*", "com.android.*")

        return EngineManager.buildAgentArgs(engineProvider.get(), task, reportFile, filters)
    }
}


/*
  To support parallel tests, both Coverage Engines work in append to data file mode.
  For this reason, before starting the tests, it is necessary to clear the file from the results of previous runs.
*/
private class BinaryReportCleanupAction(
    private val disabledProvider: Provider<Boolean>,
    private val reportFileProvider: Provider<RegularFile>,
    private val engineVariantProvider: Provider<CoverageEngineVariant>,
) : Action<Task> {
    override fun execute(task: Task) {
        if (disabledProvider.get()) {
            return
        }
        val file = reportFileProvider.get().asFile
        // always delete previous data file
        file.delete()
        if (engineVariantProvider.get().vendor == CoverageEngineVendor.INTELLIJ) {
            // IntelliJ engine expected empty file for parallel test execution.
            // Since it is impossible to know in advance whether the tests will be run in parallel, we always create an empty file.
            file.createNewFile()
        }
    }
}

private class MoveIntellijErrorLogAction(
    private val sourceFile: Provider<File>,
    private val targetFile: Provider<File>
) : Action<Task> {
    override fun execute(task: Task) {
        val origin = sourceFile.get()
        if (origin.exists() && origin.isFile) {
            origin.copyTo(targetFile.get(), true)
            origin.delete()
        }
    }
}

private fun Project.filtersProvider(
    projectConfig: KoverProjectConfig,
    taskConfig: KoverTaskExtension
): Provider<AgentFilters> {
    return provider {
        val overrideExcludes = taskConfig.excludes.get()
        val overrideIncludes = taskConfig.includes.get()
        val commonClassFilter = projectConfig.filters.classes.orNull

        if (commonClassFilter == null || overrideIncludes.isNotEmpty() || overrideExcludes.isNotEmpty()) {
            // the rules from the task take precedence over the common filters
            AgentFilters(overrideIncludes, overrideExcludes)
        } else {
            AgentFilters(commonClassFilter.includes.toList(), commonClassFilter.excludes.toList())
        }
    }
}
