/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.tasks

import kotlinx.kover.api.*
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.Internal

@Deprecated(
    message = "Class was removed in Kover API version 2, use Kover project extension instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
    level = DeprecationLevel.ERROR
)
open class KoverHtmlReportTask : DefaultTask() {
    @Deprecated(
        message = "Property was removed in Kover API version 2, use property 'htmlReport { reportDir }' in Kover project extension instead. Please refer to migration guide in order to migrate: ${KoverMigrations.MIGRATION_0_5_TO_0_6}",
        level = DeprecationLevel.ERROR
    )
    @get:Internal
    val htmlReportDir: DirectoryProperty = project.objects.directoryProperty()
}
