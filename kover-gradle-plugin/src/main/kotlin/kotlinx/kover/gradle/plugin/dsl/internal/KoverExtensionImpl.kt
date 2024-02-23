/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.internal

import kotlinx.kover.gradle.plugin.commons.KoverIllegalConfigException
import kotlinx.kover.gradle.plugin.dsl.KoverExtension
import kotlinx.kover.gradle.plugin.dsl.KoverMergingConfig
import kotlinx.kover.gradle.plugin.dsl.KoverReportConfig
import kotlinx.kover.gradle.plugin.dsl.KoverVariantsRootConfig
import kotlinx.kover.gradle.plugin.dsl.KoverVersions.JACOCO_TOOL_DEFAULT_VERSION
import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject


internal abstract class KoverExtensionImpl @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
    projectPath: String
): KoverExtension {
    internal val variants: KoverVariantsRootConfigImpl = objects.newInstance()
    internal val reports: KoverReportConfigImpl = objects.newInstance(objects, layout, projectPath)
    internal val merge: KoverMergingConfigImpl = objects.newInstance()
    internal var isMerged: Boolean = false
    internal val finalizeActions: MutableList<() -> Unit> = mutableListOf()

    init {
        @Suppress("LeakingThis")
        useJacoco.convention(false)
        @Suppress("LeakingThis")
        jacocoVersion.convention(JACOCO_TOOL_DEFAULT_VERSION)
    }

    override fun useJacoco() {
        useJacoco.set(true)
    }

    override fun useJacoco(version: String) {
        useJacoco.set(true)
        jacocoVersion.set(version)
    }

    override fun variants(block: Action<KoverVariantsRootConfig>) {
        block.execute(variants)
    }

    override fun reports(block: Action<KoverReportConfig>) {
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
