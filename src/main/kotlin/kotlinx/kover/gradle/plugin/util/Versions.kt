/*
 * Copyright 2017-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.gradle.plugin.util

internal class SemVer(private val major: Int, private val minor: Int, private val patch: Int) : Comparable<SemVer> {
    companion object {
        /**
         * Supported formats:
         *  - "1" -> 1.0.0
         *  - "1.2" -> 1.2.0
         *  - "1.2.3" -> 1.2.3
         */
        fun ofVariableOrNull(version: String): SemVer? {
            val parts = version.substringBefore('-').split(".")

            val major = parts[0].toIntOrNull()?: return null
            val minor = parts.getOrNull(1)?.toIntOrNull()?: 0
            val patch = parts.getOrNull(2)?.toIntOrNull()?: 0

            return SemVer(major, minor, patch)
        }

        fun ofThreePartOrNull(version: String): SemVer? {

            val parts = version.substringBefore('-').split(".")
            if (parts.size != 3) return null

            val major = parts[0].toIntOrNull()?: return null
            val minor = parts[1].toIntOrNull()?: return null
            val patch = parts[2].toIntOrNull()?: return null

            return SemVer(major, minor, patch)
        }
    }

    override fun compareTo(other: SemVer): Int {
        major.compareTo(other.major).takeIf { it != 0 }?.let { return it }
        minor.compareTo(other.minor).takeIf { it != 0 }?.let { return it }
        return patch.compareTo(other.patch)
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        return other is SemVer && this.compareTo(other) == 0
    }

    override fun hashCode(): Int {
        var result = major.hashCode()
        result = 31 * result + minor.hashCode()
        result = 31 * result + patch.hashCode()
        return result
    }
}


