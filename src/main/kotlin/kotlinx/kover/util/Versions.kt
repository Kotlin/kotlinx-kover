/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.util

internal class SemVer(val major: Int, val minor: Int, val patch: Int): Comparable<SemVer> {
    companion object {
        fun ofThreePartOrNull(version: String): SemVer? {
            val parts = version.split(".")
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


