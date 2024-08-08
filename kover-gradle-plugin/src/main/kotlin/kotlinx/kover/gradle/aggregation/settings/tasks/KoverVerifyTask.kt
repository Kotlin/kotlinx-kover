/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.tasks

import kotlinx.kover.features.jvm.KoverLegacyFeatures
import kotlinx.kover.gradle.plugin.commons.KoverVerificationException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

@CacheableTask
internal abstract class KoverVerifyTask : AbstractKoverTask() {
    @get:Input
    abstract val warningInsteadOfFailure: Property<Boolean>

    @get:Nested
    abstract val rules: ListProperty<VerificationRuleInput>

    init {
        // disable caching if write error to the log
        outputs.upToDateWhen { !warningInsteadOfFailure.get() }
    }

    @TaskAction
    fun generate() {
        val commonArtifacts = artifacts.elements.get().data()
        val binaryReports = commonArtifacts.values.flatMap { artifact -> artifact.reports }

        val violations = rules.get()
            .filterNot { rule -> rule.disabled }
            .flatMap { rule ->
                val outputs = commonArtifacts.values
                    .map { artifact -> artifact.filterProjectSources(rule.filters) }
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

        if (violations.isEmpty()) {
            // no errors
            return
        }

        val errorMessage = KoverLegacyFeatures.violationMessage(violations)
        if (warningInsteadOfFailure.get()) {
            logger.warn("Kover Verification Error\n$errorMessage")
        } else {
            throw KoverVerificationException(errorMessage)
        }
    }

}