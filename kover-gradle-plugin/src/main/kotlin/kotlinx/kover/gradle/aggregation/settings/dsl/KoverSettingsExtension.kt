/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.dsl

import kotlinx.kover.gradle.plugin.dsl.KoverGradlePluginDsl
import org.gradle.api.Action
import org.gradle.api.provider.SetProperty

@KoverGradlePluginDsl
public interface KoverSettingsExtension {
    fun enableCoverage()

    val reports: ReportsSettings
    fun reports(action: Action<ReportsSettings>)
}

@KoverGradlePluginDsl
public interface ReportsSettings {
    val includedProjects: SetProperty<String>
    val excludedProjects: SetProperty<String>
    val excludedClasses: SetProperty<String>
    val includedClasses: SetProperty<String>
    val excludesAnnotatedBy: SetProperty<String>
    val includesAnnotatedBy: SetProperty<String>
}
