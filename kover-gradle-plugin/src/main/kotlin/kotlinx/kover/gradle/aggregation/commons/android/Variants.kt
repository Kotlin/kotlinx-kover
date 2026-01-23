/*
 * Copyright 2017-2026 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.aggregation.commons.android

import kotlinx.kover.gradle.aggregation.commons.utils.DynamicBean
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.io.File
import kotlin.collections.map

/**
 * Info for build variant of AGP since 9.0.0
 */
internal data class AndroidVariantInfo(
    @get:Input
    val name: String,

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val sourceDirs: Collection<File>
)

internal fun DynamicBean.convertAggregatedVariant(): AndroidVariantInfo {
    val variantName = value<String>("name")

    val sourceDirs = bean("sources").beanOrNull("kotlin")?.value<Provider<Collection<Directory>>>("all")?.get()?.map { it.asFile }?.toSet() ?: emptySet()

    return AndroidVariantInfo(variantName, sourceDirs)
}
