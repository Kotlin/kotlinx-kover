/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.dsl.intern

import kotlinx.kover.gradle.aggregation.settings.dsl.BoundSettings
import kotlinx.kover.gradle.aggregation.settings.dsl.ProjectVerificationRuleSettings
import kotlinx.kover.gradle.aggregation.settings.dsl.ReportFiltersSettings
import kotlinx.kover.gradle.aggregation.settings.dsl.VerificationRuleSettings
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

internal abstract class VerificationRuleSettingsImpl: VerificationRuleSettings {
    @get:Inject
    abstract val objects: ObjectFactory

    @Suppress("LeakingThis")
    override val filters: ReportFiltersSettings = objects.newInstance()

    override fun bound(action: Action<BoundSettings>) {
        val bound = objects.newInstance<BoundSettings>()

        bound.coverageUnits.convention(CoverageUnit.LINE)
        bound.aggregationForGroup.convention(AggregationType.COVERED_PERCENTAGE)

        action.execute(bound)
        bounds.add(bound)
    }
}

internal abstract class ProjectVerificationRuleSettingsImpl @Inject constructor(
    override val projectName: String,
    override val projectPath: String
): VerificationRuleSettingsImpl(), ProjectVerificationRuleSettings {


}