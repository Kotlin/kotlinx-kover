package kotlinx.kover.examples.merged.subproject

import kotlin.math.*

class SubprojectSecondClass {
    fun formatDouble(d: Double): String {
        if (d.roundToInt().toDouble() == d) {
            return "INTEGER=${d.roundToInt()}"
        } else {
            return "FRACTIONAL=$d"
        }
    }

    fun printClass() {
        val name = this::class.qualifiedName
        println(name)
    }
}
