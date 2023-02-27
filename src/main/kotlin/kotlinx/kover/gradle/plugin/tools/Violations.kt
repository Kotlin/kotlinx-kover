/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.tools

import kotlinx.kover.gradle.plugin.dsl.*
import java.math.*

internal data class RuleViolations(
    val entityType: GroupingEntityType,
    val bounds: List<BoundViolations>,
    val name: String? = null
)

internal data class BoundViolations(
    val isMax: Boolean,
    val expectedValue: BigDecimal,
    val actualValue: BigDecimal,
    val metric: MetricType,
    val aggregation: AggregationType,
    val entityName: String? = null
)
