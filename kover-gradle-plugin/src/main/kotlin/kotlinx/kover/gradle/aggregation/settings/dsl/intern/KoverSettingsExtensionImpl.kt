/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.dsl.intern

import kotlinx.kover.gradle.aggregation.settings.dsl.KoverSettingsExtension
import kotlinx.kover.gradle.aggregation.settings.dsl.ReportsSettings
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

@Suppress("LeakingThis")
internal abstract class KoverSettingsExtensionImpl @Inject constructor(
    objects: ObjectFactory
) : KoverSettingsExtension {
    abstract val coverageIsEnabled: Property<Boolean>

    override val reports: ReportsSettings = objects.newInstance<ReportsSettingsImpl>()

    init {
        coverageIsEnabled.convention(false)
    }

    override fun enableCoverage() {
        coverageIsEnabled.set(true)
    }

    override fun reports(action: Action<ReportsSettings>) {
        action.execute(reports)
    }
}