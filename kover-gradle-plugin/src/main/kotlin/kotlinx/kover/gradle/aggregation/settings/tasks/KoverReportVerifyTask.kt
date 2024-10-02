/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.tasks

import kotlinx.kover.features.jvm.KoverLegacyFeatures
import kotlinx.kover.gradle.plugin.commons.KoverVerificationException
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

@CacheableTask
internal abstract class KoverReportVerifyTask : AbstractKoverTask() {
    @get:Input
    abstract val warningInsteadOfFailure: Property<Boolean>

    @get:Nested
    abstract val rulesByProjectPath: MapProperty<String, List<VerificationRuleInput>>

    init {
        // disable caching if we emit only warnings
        outputs.upToDateWhen { !warningInsteadOfFailure.get() }

        @Suppress("LeakingThis")
        onlyIf { rulesByProjectPath.get().isNotEmpty() }
    }

    @TaskAction
    fun generate() {
        val commonArtifacts = artifacts.elements.get().data()
        val binaryReports = commonArtifacts.values.flatMap { artifact -> artifact.reports }

        val violations = rulesByProjectPath.get().mapValues { (path, rules) ->
            rules
                .filterNot { rule -> rule.disabled }
                .flatMap { rule ->
                    val outputs = commonArtifacts.values
                        .map { artifact ->
                            artifact.filterProjectSources(FiltersInput(includedProjects = setOf(path)))
                        }
                        .flatMap { artifact ->
                            artifact.compilations.flatMap { compilation -> compilation.value.outputDirs }
                        }

                    KoverLegacyFeatures.verify(
                        listOf(rule.toExternal()),
                        temporaryDir,
                        rule.filters.toExternalFilters(),
                        binaryReports,
                        outputs
                    )
                }
        }.filter {
            ruleViolations -> ruleViolations.value.isNotEmpty()
        }

        if (violations.isEmpty()) {
            // no errors
            return
        }

        val stringBuilder = StringBuilder()
        violations.forEach { (_, projectViolations) ->
            stringBuilder.appendLine(KoverLegacyFeatures.violationMessage(projectViolations))
        }

        val errorMessage = stringBuilder.toString()
        if (warningInsteadOfFailure.get()) {
            logger.warn("Kover Verification Error\n$errorMessage")
        } else {
            throw KoverVerificationException(errorMessage)
        }
    }
}