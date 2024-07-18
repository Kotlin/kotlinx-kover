/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.dsl.intern

import kotlinx.kover.gradle.aggregation.settings.dsl.ReportFiltersSettings
import kotlinx.kover.gradle.aggregation.settings.dsl.VerificationRuleSettings
import kotlinx.kover.gradle.aggregation.settings.dsl.VerifySettings
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

internal abstract class VerifySettingsImpl @Inject constructor(
    private val commonFilters: ReportFiltersSettings,
) : VerifySettings {

    @get:Inject
    abstract val objects: ObjectFactory

    init {
        @Suppress("LeakingThis")
        warningInsteadOfFailure.convention(false)
    }

    override fun rule(name: String, action: Action<VerificationRuleSettings>) {
        val rule = newRule()
        rule.name.convention(name)
        action.execute(rule)
        rules.add(rule)
    }

    override fun rule(action: Action<VerificationRuleSettings>) {
        val rule = newRule()
        action.execute(rule)
        rules.add(rule)
    }

    private fun newRule(): VerificationRuleSettings {
        val rule: VerificationRuleSettings = objects.newInstance<VerificationRuleSettingsImpl>()
        rule.filters.inheritFrom(commonFilters)
        rule.disabled.convention(false)
        rule.groupBy.convention(GroupingEntityType.APPLICATION)
        return rule
    }

    private fun ReportFiltersSettings.inheritFrom(other: ReportFiltersSettings) {
        includedProjects.convention(other.includedProjects)
        excludedProjects.convention(other.excludedProjects)
        includedClasses.convention(other.includedClasses)
        excludedClasses.convention(other.excludedClasses)
        includesAnnotatedBy.convention(other.includesAnnotatedBy)
        excludesAnnotatedBy.convention(other.excludesAnnotatedBy)
        includesInheritedFrom.convention(other.includesInheritedFrom)
        excludesInheritedFrom.convention(other.excludesInheritedFrom)
    }
}