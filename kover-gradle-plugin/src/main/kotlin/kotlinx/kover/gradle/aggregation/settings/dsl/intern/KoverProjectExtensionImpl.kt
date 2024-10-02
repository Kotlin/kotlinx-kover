/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.settings.dsl.intern

import kotlinx.kover.gradle.aggregation.settings.dsl.KoverProjectExtension
import kotlinx.kover.gradle.aggregation.settings.dsl.ProjectInstrumentationSettings
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

internal abstract class KoverProjectExtensionImpl: KoverProjectExtension {
    @get:Inject
    abstract val objects: ObjectFactory

    override val instrumentation: ProjectInstrumentationSettings = objects.newInstance<ProjectInstrumentationSettings>()

    override fun instrumentation(action: Action<ProjectInstrumentationSettings>) {
        action.execute(instrumentation)
    }
}