package kotlinx.kover.maven.plugin.testing

class Main {
    fun used(flag: Boolean) {
        if (flag) {
            println("used")
        }
        println("false")
    }

    fun unused() {
        println("unused")
    }
}