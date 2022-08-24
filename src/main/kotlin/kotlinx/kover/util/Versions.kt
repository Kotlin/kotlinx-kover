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
        val majorCompare = major.compareTo(other.major)
        if (majorCompare != 0) return majorCompare

        val minorCompare = minor.compareTo(other.minor)
        if (minorCompare != 0) return minorCompare

        return patch.compareTo(other.patch)
    }
}


