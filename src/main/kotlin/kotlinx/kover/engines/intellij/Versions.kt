/*
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.kover.engines.intellij

val minimalIntellijVersion = IntellijEngineVersion(1, 0, 647)
val defaultIntellijVersion = IntellijEngineVersion(1, 0, 656)

data class IntellijEngineVersion(val major: Int, val minor: Int, val build: Int): Comparable<IntellijEngineVersion> {
    override fun compareTo(other: IntellijEngineVersion): Int {
        var compared = major.compareTo(other.major)
        if (compared != 0) {
            return compared
        }
        compared = minor.compareTo(other.minor)
        if (compared != 0) {
            return compared
        }
        return build.compareTo(other.build)
    }

    override fun toString(): String {
        return "$major.$minor.$build"
    }

    companion object {
        fun parseOrNull(string: String): IntellijEngineVersion? {
            val parts = string.split('.')
            if (parts.size != 3) return null
            val major = parts[0].toIntOrNull() ?: return null
            val minor = parts[1].toIntOrNull() ?: return null
            val build = parts[2].toIntOrNull() ?: return null

            return IntellijEngineVersion(major, minor, build)
        }
    }
}


