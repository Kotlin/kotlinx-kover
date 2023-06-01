/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:Suppress("UNUSED_PARAMETER")

package kotlinx.kover.api

import kotlinx.kover.gradle.plugin.commons.KoverMigrations.MIGRATION_0_6_TO_0_7

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class KoverProjectConfig

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class KoverProjectFilters

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class KoverProjectInstrumentation

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class KoverProjectXmlConfig

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class KoverProjectHtmlConfig

@Deprecated(
    message = "Kover merged report config was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.WARNING
)
public open class KoverMergedConfig {

    @Deprecated(
        message = "Kover merged report config was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
        level = DeprecationLevel.ERROR
    )
    public fun enable() {
    }

    @Deprecated(
        message = "Kover merged report config was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
        level = DeprecationLevel.ERROR
    )
    public fun filters(config: () -> Unit) {
    }

    @Deprecated(
        message = "Kover merged report config was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
        level = DeprecationLevel.ERROR
    )
    public fun xmlReport(config: () -> Unit) {
    }

    @Deprecated(
        message = "Kover merged report config was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
        level = DeprecationLevel.ERROR
    )
    public fun htmlReport(config: () -> Unit) {
    }

    @Deprecated(
        message = "Kover merged report config was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
        level = DeprecationLevel.ERROR
    )
    public fun verify(config: () -> Unit) {
    }
}

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class KoverMergedFilters

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class KoverMergedXmlConfig

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class KoverMergedHtmlConfig

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class KoverProjectsFilter

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class KoverVerifyConfig

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class KoverClassFilter

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class KoverAnnotationFilter

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class VerificationRule

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public open class VerificationBound

@Deprecated(
    message = "Class was removed, use class 'kotlinx.kover.gradle.plugin.dsl.GroupingEntityType' instead. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public enum class VerificationTarget

@Deprecated(
    message = "Class was removed, use class 'kotlinx.kover.gradle.plugin.dsl.MetricType' instead. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public enum class CounterType

@Deprecated(
    message = "Class was removed, use class 'kotlinx.kover.gradle.plugin.dsl.AggregationType' instead. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public enum class VerificationValueType
