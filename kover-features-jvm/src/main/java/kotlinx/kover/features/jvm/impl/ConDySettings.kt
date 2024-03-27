/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.features.jvm.impl


/**
 * Internal class to control JVM ConDy settings.
 */
internal object ConDySettings {
    private const val CONDY_SYSTEM_PARAM_NAME = "coverage.condy.enable"

    /**
     * Disable JVM ConDy during instrumentation.
     *
     * @return previous value of ConDy setting
     */
    fun disableConDy(): String? {
        // disable ConDy for offline instrumentations
        return System.setProperty(CONDY_SYSTEM_PARAM_NAME, "false")
    }

    /**
     * Restore previous value of JVM ConDy setting.
     *
     * Returns prevValue new setting value.
     */
    fun restoreConDy(prevValue: String?) {
        if (prevValue == null) {
            System.clearProperty(CONDY_SYSTEM_PARAM_NAME)
        } else {
            System.setProperty(CONDY_SYSTEM_PARAM_NAME, prevValue)
        }
    }
}
