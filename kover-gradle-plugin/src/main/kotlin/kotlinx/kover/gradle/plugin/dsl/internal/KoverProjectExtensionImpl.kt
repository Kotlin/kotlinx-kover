/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.internal

import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.dsl.*
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
    internal val current: KoverCurrentProjectVariantsConfigImpl = objects.newInstance()
    internal val reports: KoverReportsConfigImpl = objects.newInstance(objects, layout, projectPath)
    internal val merge: KoverMergingConfigImpl = objects.newInstance()
    internal abstract val koverDisabled: Property<Boolean>
    internal var isMerged: Boolean = false
    internal val finalizeActions: MutableList<() -> Unit> = mutableListOf()

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
        block.execute(current)
    }

    override fun reports(block: Action<KoverReportsConfig>) {
        block.execute(reports)
    }

    override fun merge(block: Action<KoverMergingConfig>) {
        if (isMerged) {
            throw KoverIllegalConfigException("An attempt to re-invoke the 'merge' block. Only one merging config is allowed")
        }
        isMerged = true
        block.execute(merge)
    }

    internal fun beforeFinalize(action: () -> Unit) {
        finalizeActions += action
    }
}
