package org.jetbrains.kover.test.functional.verification.subpackage

class SubFullyCovered {
    fun function0(i: Int): String {
        val j = i + 2
        println("function0")
        return "result=$j"
    }

    fun function1(i: Int): String {
        val j = i + 2
        println("function0")
        return "result=$j"
    }

    fun name(): String? {
        return this::class.simpleName
    }
}

class SubPartiallyCoveredFirst {
    fun function0(i: Int): String {
        val j = i + 2
        println("function0")
        return "result=$j"
    }

    fun function1(i: Int): String {
        val j = i + 2
        println("function1")
        return "result=$j"
    }

    fun name(): String? {
        return this::class.simpleName
    }
}

class SubPartiallyCoveredSecond {
    fun function0(i: Int): String {
        val j = i + 2
        if (i > 0) {
            println("GTZ")
        } else if (i == 0) {
            println("EZ")
        } else {
            println("LEZ")
        }
        println("function0")
        return "result=$j"
    }

    fun function1(i: Int): String {
        val j = i + 2
        if (i > 0) {
            println("GTZ")
        } else if (i == 0) {
            println("EZ")
        } else {
            println("LEZ")
        }
        println("function1")
        return "result=$j"
    }

    fun name(): String? {
        return this::class.simpleName
    }
}
class SubUncovered {
    fun function0(i: Int): String {
        val j = i + 2
        println("function0")
        return "result=$j"
    }

    fun function1(i: Int): String {
        val j = i + 2
        println("function1")
        return "result=$j"
    }

    fun name(): String? {
        return this::class.simpleName
    }
}
