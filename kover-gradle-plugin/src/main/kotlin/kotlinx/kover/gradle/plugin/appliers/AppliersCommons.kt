/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import kotlinx.kover.gradle.plugin.commons.DEFAULT_KOVER_VARIANT_NAME
import kotlinx.kover.gradle.plugin.commons.htmlReportPath
import kotlinx.kover.gradle.plugin.commons.xmlReportPath
import kotlinx.kover.gradle.plugin.dsl.internal.KoverDefaultReportsConfigImpl
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance

internal fun ObjectFactory.defaultReports(layout: ProjectLayout): KoverDefaultReportsConfigImpl {
    val buildDir = layout.buildDirectory

    val reports = newInstance<KoverDefaultReportsConfigImpl>(this)

    reports.xml {
        setReportFile(buildDir.file(xmlReportPath(DEFAULT_KOVER_VARIANT_NAME)))
        onCheck = false
    }

    reports.html {
        setReportDir(buildDir.dir(htmlReportPath(DEFAULT_KOVER_VARIANT_NAME)))
        onCheck = false
    }

    reports.verify {
        onCheck = true
    }

    return reports
}

