/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings

import kotlinx.kover.gradle.aggregation.settings.dsl.intern.KoverSettingsExtensionImpl
import org.gradle.api.provider.HasMultipleValues
import org.gradle.api.provider.ProviderFactory


internal object KoverParametersProcessor {
    fun process(settingsExtension: KoverSettingsExtensionImpl, providers: ProviderFactory) {
        if (providers.gradleProperty("kover").isPresent) {
            settingsExtension.coverageIsEnabled.set(true)
        }

        settingsExtension.reports.includedProjects.readAppendableArgument(providers, "kover.classes.from")
        settingsExtension.reports.excludedProjects.readAppendableArgument(providers, "kover.classes.from.excludes")
        settingsExtension.reports.excludedClasses.readAppendableArgument(providers, "kover.classes.excludes")
        settingsExtension.reports.includedClasses.readAppendableArgument(providers, "kover.classes.includes")
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