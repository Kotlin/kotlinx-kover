package kotlinx.kover.examples.merged.subproject.utils

class SubprojectUtils {
    fun minus(a: Int, b: Int): Int {
        if (b < 0) return 0
        return a - b
    }
}
