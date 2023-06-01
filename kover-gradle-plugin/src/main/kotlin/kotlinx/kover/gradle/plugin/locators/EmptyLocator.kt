/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl


internal class EmptyLocator : CompilationKitLocator {
    override fun locate(koverExtension: KoverProjectExtensionImpl): ProjectCompilation {
        return ProjectCompilation(AppliedKotlinPlugin(null))
    }
}
