/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import java.io.File


open class KoverTaskExtension(objects: ObjectFactory) {
    @Input
    var enabled: Boolean = true

    @Input
    var useJacoco: Boolean = false

    @Input
    var xmlReport: Boolean = true

    @Input
    var htmlReport: Boolean = false

    @OutputFile
    var xmlReportFile: Property<File> = objects.property(File::class.java)

    @OutputFile
    var htmlReportFile: Property<File> = objects.property(File::class.java)

    @OutputFile
    var binaryFile: Property<File> = objects.property(File::class.java)

    @Optional
    @Input
    var includes: List<String> = emptyList()

    @Optional
    @Input
    var excludes: List<String> = emptyList()
}
