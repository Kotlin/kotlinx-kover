/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import kotlinx.kover.gradle.plugin.commons.KoverMigrations

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
    level = DeprecationLevel.WARNING
)
public open class KoverTaskExtension {
    @Deprecated(
        message = "Kover test task config was removed. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public val isDisabled: Boolean = false

    @Deprecated(
        message = "Kover test task config was removed. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public val reportFile: Nothing? = null

    @Deprecated(
        message = "Kover test task config was removed. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public val includes: MutableList<String> = mutableListOf()

    @Deprecated(
        message = "Kover test task config was removed. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_6_TO_0_7}",
        level = DeprecationLevel.ERROR
    )
    public val excludes: MutableList<String> = mutableListOf()
}

