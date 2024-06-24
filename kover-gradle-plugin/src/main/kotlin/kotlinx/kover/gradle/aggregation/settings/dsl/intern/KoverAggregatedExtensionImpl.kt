/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.dsl.intern

import org.gradle.api.provider.SetProperty

internal abstract class KoverAggregatedExtensionImpl {
    internal abstract val projects: SetProperty<String>
    internal abstract val excludedProjects: SetProperty<String>
    internal abstract val excludedClasses: SetProperty<String>
}