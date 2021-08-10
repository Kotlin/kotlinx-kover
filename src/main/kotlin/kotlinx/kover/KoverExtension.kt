/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

open class KoverExtension(objects: ObjectFactory) {
    val intellijAgentVersion: Property<String> = objects.property(String::class.java)
    val jacocoAgentVersion: Property<String> = objects.property(String::class.java)
}
