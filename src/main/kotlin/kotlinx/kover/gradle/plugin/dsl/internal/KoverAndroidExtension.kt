/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.dsl.internal

import kotlinx.kover.gradle.plugin.dsl.*
import org.gradle.api.*
import org.gradle.api.model.*
import org.gradle.kotlin.dsl.*
import javax.inject.*

internal open class KoverAndroidExtensionImpl @Inject constructor(private val objects: ObjectFactory): KoverAndroidExtension {
    internal val common: KoverCommonReportExtensionImpl = objects.newInstance()
    internal val reports: MutableMap<String, KoverReportExtensionImpl> = mutableMapOf()

    override fun common(config: Action<KoverCommonReportExtension>) {
        config(common)
    }

    override fun report(buildVariantName: String, config: Action<KoverReportExtension>) {
        val report = reports.getOrPut(buildVariantName) { objects.newInstance(objects) }
        config(report)
    }

}
