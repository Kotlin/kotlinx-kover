package kotlinx.kover.examples.merged

class ExampleClass {
    fun formatInt(i: Int): String {
        if (i == 0) return "ZERO"
        return if (i > 0) {
            "POSITIVE=$i"
        } else {
            "NEGATIVE=${-i}"
        }
    }

    fun printClass() {
        val name = this::class.qualifiedName
        println(name)
    }
}
