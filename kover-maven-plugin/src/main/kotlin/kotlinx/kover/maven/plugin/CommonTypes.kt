/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.maven.plugin

import kotlinx.kover.features.jvm.AggregationType
import kotlinx.kover.features.jvm.CoverageUnit
import kotlinx.kover.features.jvm.GroupingBy

data class MavenReportFilters(
    var excludes: MavenReportFilter = MavenReportFilter(),
    var includes: MavenReportFilter = MavenReportFilter()
) {
    val isEmpty: Boolean get() = excludes.isEmpty && includes.isEmpty
}

data class MavenReportFilter(
    val classes: MutableList<String> = mutableListOf(),
    val annotatedBy: MutableList<String> = mutableListOf(),
    val inheritedFrom: MutableList<String> = mutableListOf(),
    val projects: MutableList<String> = mutableListOf()
) {
    val isEmpty: Boolean get() = classes.isEmpty() && annotatedBy.isEmpty() && inheritedFrom.isEmpty()
}


data class MavenRule(
    var name: String = "",
    var filters: MavenRuleFilters? = null,
    var groupBy: GroupingBy = GroupingBy.APPLICATION,
    val bounds: List<MavenBound> = emptyList()
)

data class MavenBound(
    var minValue: String? = null,
    var maxValue: String? = null,
    var coverageUnits: CoverageUnit = CoverageUnit.LINE,
    var aggregationForGroup: AggregationType = AggregationType.COVERED_PERCENTAGE
)

data class MavenRuleFilters(var includes: MavenRuleFilter? = null, var excludes: MavenRuleFilter? = null)

data class MavenRuleFilter(
    val classes: MutableList<String> = mutableListOf(),
    val annotatedBy: MutableList<String> = mutableListOf(),
    val inheritedFrom: MutableList<String> = mutableListOf()
)
