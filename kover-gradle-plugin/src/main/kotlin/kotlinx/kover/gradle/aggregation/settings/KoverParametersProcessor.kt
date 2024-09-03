/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings

import kotlinx.kover.gradle.aggregation.settings.dsl.VerificationRuleSettings
import kotlinx.kover.gradle.aggregation.settings.dsl.intern.KoverSettingsExtensionImpl
import org.gradle.api.provider.HasMultipleValues
import org.gradle.api.provider.ProviderFactory


internal object KoverParametersProcessor {
    fun process(settingsExtension: KoverSettingsExtensionImpl, providers: ProviderFactory) {
        val koverProperty = providers.gradleProperty("kover")
        if (koverProperty.isPresent) {
            val disabled = koverProperty.get().equals("false", ignoreCase = true)
            settingsExtension.coverageIsEnabled.set(!disabled)
        }
        val verifyWarn = providers.gradleProperty("kover.verify.warn")
        if (verifyWarn.isPresent) {
            verifyWarn.get().lowercase().toBooleanStrictOrNull()?.also { warn ->
                settingsExtension.reports.verify.warningInsteadOfFailure.set(warn)
            }
        }
        val ruleSettings: MutableList<VerificationRuleSettings.() -> Unit> = mutableListOf()
        val minVerify = providers.gradleProperty("kover.verify.min")
        if (minVerify.isPresent) {
            minVerify.get().toIntOrNull()?.also { min ->
                ruleSettings += { bound { minValue.set(min) } }
            }
        }
        val maxVerify = providers.gradleProperty("kover.verify.max")
        if (maxVerify.isPresent) {
            maxVerify.get().toIntOrNull()?.also { max ->
                ruleSettings += { bound { maxValue.set(max) } }
            }
        }
        if (ruleSettings.isNotEmpty()) {
            settingsExtension.reports.verify.rule("CLI parameters") {
                ruleSettings.forEach { setting -> setting() }
            }
        }




        settingsExtension.reports.includedProjects.readAppendableArgument(providers, "kover.projects.includes")
        settingsExtension.reports.excludedProjects.readAppendableArgument(providers, "kover.projects.excludes")
        settingsExtension.reports.excludedClasses.readAppendableArgument(providers, "kover.classes.excludes")
        settingsExtension.reports.includedClasses.readAppendableArgument(providers, "kover.classes.includes")
        settingsExtension.reports.excludesAnnotatedBy.readAppendableArgument(providers, "kover.classes.excludesAnnotated")
        settingsExtension.reports.includesAnnotatedBy.readAppendableArgument(providers, "kover.classes.includesAnnotated")
    }

    private fun HasMultipleValues<String>.readAppendableArgument(providers: ProviderFactory, propertyName: String) {
        val propertyProvider = providers.gradleProperty(propertyName)
        if (propertyProvider.isPresent) {
            val arg = propertyProvider.get().parseCollection()
            if (!arg.append) {
                empty()
            }
            addAll(arg.values)
        }
    }

    private fun String.parseCollection(): ArgCollection {
        val append = startsWith('+')
        val str = if (append) substring(1) else this
        val values = str.split(',')
        return ArgCollection(append, values)
    }

    private data class ArgCollection(val append: Boolean, val values: List<String>)
}