/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.features.jvm

import kotlinx.kover.features.jvm.impl.OfflineInstrumenterImpl
import kotlinx.kover.features.jvm.impl.wildcardsToRegex
import java.util.*

/**
 * A class for using features via Java calls.
 */
public object KoverFeatures {
    /**
     * Getting version of Kover used in these utilities.
     */
    public val version: String = readVersion()

    /**
     * Converts a Kover [template] string to a regular expression string.
     *ё
     * Replaces characters `*` to `.*`, `#` to `[^.]*` and `?` to `.` regexp characters.
     * All special characters of regular expressions are also escaped.
     */
    public fun koverWildcardToRegex(template: String): String {
        return template.wildcardsToRegex()
    }

    /**
     * Create instance to instrument already compiled class-files.
     *
     * @return instrumenter for offline instrumentation.
     */
    public fun createOfflineInstrumenter(): OfflineInstrumenter {
        return OfflineInstrumenterImpl(false)
    }

    private fun readVersion(): String {
        var version = "unrecognized"
        // read version from file in resources
        try {
            KoverFeatures::class.java.classLoader.getResourceAsStream("kover.version").use { stream ->
                if (stream != null) {
                    version = Scanner(stream).nextLine()
                }
            }
        } catch (e: Throwable) {
            // can't read
        }
        return version
    }
}