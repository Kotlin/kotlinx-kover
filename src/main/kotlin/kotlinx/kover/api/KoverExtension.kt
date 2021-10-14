/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.api

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class KoverExtension(objects: ObjectFactory) {
    /**
     * Specifies the version of Intellij-coverage dependency.
     */
    val intellijEngineVersion: Property<String> = objects.property(String::class.java)

    /**
     * Specifies the version of JaCoCo dependency.
     */
    val jacocoEngineVersion: Property<String> = objects.property(String::class.java)
}
