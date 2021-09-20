/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.File


open class KoverTaskExtension(objects: ObjectFactory) {
    var enabled: Boolean = true
    var useJacoco: Boolean = false
    var xmlReport: Boolean = true
    var htmlReport: Boolean = false
    var xmlReportFile: Property<File> = objects.property(File::class.java)
    var htmlReportDir: DirectoryProperty = objects.directoryProperty()
    var binaryFile: Property<File> = objects.property(File::class.java)
    var includes: List<String> = emptyList()
    var excludes: List<String> = emptyList()
}
