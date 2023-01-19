/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.locators

import kotlinx.kover.gradle.plugin.commons.*
import kotlinx.kover.gradle.plugin.commons.KoverSetup
import kotlinx.kover.gradle.plugin.dsl.internal.KoverProjectExtensionImpl
import org.gradle.api.*
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*


internal class EmptyLocator(private val project: Project) : SetupLocator {
    override val kotlinPlugin: AppliedKotlinPlugin = AppliedKotlinPlugin(null)

    override fun locate(koverExtension: KoverProjectExtensionImpl): List<KoverSetup<*>> {
        val setup = KoverSetup(
            // provider with empty directories and compile tasks
            project.provider { KoverSetupBuild() },
            //empty collection of test tasks
            project.tasks.withType<Test>().matching { false }
        )

        return listOf(setup)
    }
}
