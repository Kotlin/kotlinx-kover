/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.internal

import kotlinx.kover.gradle.plugin.dsl.KoverCurrentProjectVariantsConfig
import kotlinx.kover.gradle.plugin.dsl.KoverMergingConfig
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportsConfig
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.JACOCO_TOOL_DEFAULT_VERSION
import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject


internal abstract class KoverProjectExtensionImpl @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
    projectPath: String
): KoverProjectExtension {
    internal abstract val koverDisabled: Property<Boolean>
    internal val finalizeActions: MutableList<() -> Unit> = mutableListOf()

    override val reports: KoverReportsConfigImpl = objects.newInstance(objects, layout, projectPath)
    override val merge: KoverMergingConfigImpl = objects.newInstance()
    override val currentProject: KoverCurrentProjectVariantsConfigImpl = objects.newInstance()

    init {
        @Suppress("LeakingThis")
        useJacoco.convention(false)
        @Suppress("LeakingThis")
        jacocoVersion.convention(JACOCO_TOOL_DEFAULT_VERSION)
        @Suppress("LeakingThis")
        koverDisabled.convention(false)
    }

    override fun disable() {
        koverDisabled.set(true)
    }

    override fun useJacoco() {
        useJacoco.set(true)
    }

    override fun useJacoco(version: String) {
        useJacoco.set(true)
        jacocoVersion.set(version)
    }

    override fun currentProject(block: Action<KoverCurrentProjectVariantsConfig>) {
        block.execute(currentProject)
    }

    override fun reports(block: Action<KoverReportsConfig>) {
        block.execute(reports)
    }

    override fun merge(block: Action<KoverMergingConfig>) {
        block.execute(merge)

        merge.configured = true
    }

    internal fun beforeFinalize(action: () -> Unit) {
        finalizeActions += action
    }
}
