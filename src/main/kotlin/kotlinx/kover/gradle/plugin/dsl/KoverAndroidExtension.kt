/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl

import org.gradle.api.*

public interface KoverAndroidExtension {
    public fun report(buildVariantName: String, config: Action<KoverReportExtension>)
}
