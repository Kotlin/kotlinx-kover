/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import kotlinx.kover.appliers.*
import org.gradle.api.*

class KoverPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.applyToProject()
        target.applyMerged()
    }
}
