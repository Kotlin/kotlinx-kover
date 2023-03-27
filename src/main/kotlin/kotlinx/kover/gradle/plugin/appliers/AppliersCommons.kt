/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.appliers

import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.testing.Test

/**
 * Add online instrumentation to all JVM test tasks.
 */
internal fun TaskCollection<Test>.configureTests(data: InstrumentationData): TaskCollection<Test> {
    configureEach {
        JvmTestTaskApplier(this, data).apply()
    }

    return this
}