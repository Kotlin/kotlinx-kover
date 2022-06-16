/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.appliers

import kotlinx.kover.api.*
import kotlinx.kover.engines.commons.*
import kotlinx.kover.tasks.*
import org.gradle.api.*
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.testing.*
import org.gradle.process.*
import java.io.*


internal fun Test.applyToTestTask(
    projectExtension: KoverProjectConfig,
    engineProvider: Provider<EngineDetails>
) {
    val extension = createTaskExtension(projectExtension)

    val disabledProvider = project.provider {
        (projectExtension.isDisabled.get() || extension.isDisabled.get() ||
                projectExtension.instrumentation.excludeTasks.contains(name))
    }

    jvmArgumentProviders.add(CoverageArgumentProvider(this, engineProvider, disabledProvider, extension))

    val sourceErrorProvider = project.provider {
        File(extension.reportFile.get().asFile.parentFile, "coverage-error.log")
    }
    val targetErrorProvider = project.layout.buildDirectory.file("kover/errors/$name.log").map { it.asFile }

    doFirst(BinaryReportCleanupAction(disabledProvider, extension.reportFile, engineProvider.map { e -> e.variant }))
    doLast(MoveIntellijErrorLogAction(sourceErrorProvider, targetErrorProvider))
}

private fun Task.createTaskExtension(projectExtension: KoverProjectConfig): KoverTaskExtension {
    val taskExtension =
        extensions.create(KoverNames.TASK_EXTENSION_NAME, KoverTaskExtension::class.java, project.objects)

    taskExtension.isDisabled.set(false)
    taskExtension.reportFile.set(project.provider {
        val engine = projectExtension.engine.get()
        val suffix = if (engine.vendor == CoverageEngineVendor.INTELLIJ) ".ic" else ".exec"
        project.layout.buildDirectory.get().file("kover/$name$suffix")
    })

    return taskExtension
}


private class CoverageArgumentProvider(
    private val task: Task,
    @get:Nested val engineProvider: Provider<EngineDetails>,
    @get:Input val disabledProvider: Provider<Boolean>,
    @get:Nested val taskExtension: KoverTaskExtension
) : CommandLineArgumentProvider, Named {
    @Internal
    override fun getName(): String {
        return "koverArgumentsProvider"
    }

    override fun asArguments(): MutableIterable<String> {
        if (disabledProvider.get()) {
            return mutableListOf()
        }

        val reportFile = taskExtension.reportFile.get().asFile
        val classFilter = KoverClassFilter()

        classFilter.includes += taskExtension.includes.get()
        classFilter.excludes += taskExtension.excludes.get()
        /*
            The instrumentation of android classes often causes errors when using third-party
            frameworks (see https://github.com/Kotlin/kotlinx-kover/issues/89).

            Because android classes are not part of the project, in any case they do not get into the report,
            and they can be excluded from instrumentation.

            FIXME Remove this code if the IntelliJ Agent stops changing project classes during instrumentation
        */
        classFilter.excludes += "android.*"
        classFilter.excludes += "com.android.*"

        return EngineManager.buildAgentArgs(engineProvider.get(), task, reportFile, classFilter)
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
