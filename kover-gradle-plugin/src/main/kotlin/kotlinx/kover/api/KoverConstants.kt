/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import kotlinx.kover.gradle.plugin.commons.KoverMigrations.MIGRATION_0_6_TO_0_7

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public object KoverNames

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public object KoverPaths

@Deprecated(
    message = "Class was removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public object KoverMigrations
