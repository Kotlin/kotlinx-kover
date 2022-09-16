package kotlinx.kover.examples.merged.utils

class MergedUtils {
    fun sum(a: Int, b: Int): Int {
        if (a < 0 && b < 0) return 0
        return a + b
    }
}
