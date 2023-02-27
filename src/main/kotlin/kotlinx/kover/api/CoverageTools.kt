/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.KoverMigrations.MIGRATION_0_6_TO_0_7
import org.gradle.api.tasks.*


// DEPRECATIONS
// TODO delete deprecations in 0.8.x
@Deprecated(
    message = "Class was renamed removed. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public sealed class CoverageEngineVariant

@Deprecated(
    message = "Class has been replaced by using a function [useKoverTool(String)]. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public class IntellijEngine(@Suppress("UNUSED_PARAMETER") version: String)

@Deprecated(
    message = "Class has been replaced by using a function [useKoverTool()]. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public object DefaultIntellijEngine

@Deprecated(
    message = "Class has been replaced by using a function [useJacocoTool(String)]. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public class JacocoEngine(@Suppress("UNUSED_PARAMETER") version: String)

@Deprecated(
    message = "Class has been replaced by using a function [useJacocoTool()]. Please refer to migration guide in order to migrate: $MIGRATION_0_6_TO_0_7",
    level = DeprecationLevel.ERROR
)
public object DefaultJacocoEngine
