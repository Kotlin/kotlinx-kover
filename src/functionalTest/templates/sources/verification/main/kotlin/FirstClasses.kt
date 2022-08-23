package org.jetbrains.kover.test.functional.verification

class FullyCovered {
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

class PartiallyCoveredFirst {
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
        println("function1")
        return "result=$j"
    }

    fun name(): String? {
        return this::class.simpleName
    }
}

class PartiallyCoveredSecond {
    fun function0(i: Int): String {
        val j = i + 2
        println("function0")
        return "result=$j"
    }

    fun function1(i: Int): String {
        val j = i + 2
        println("function1")
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
class Uncovered {
    fun function0(i: Int): String {
        val j = i + 2
        println("function0")
        return "result=$j"
    }

    fun function1(i: Int): String {
        val j = i + 2
        println("function1")
        if (i > 0) {
            println("GTZ")
        } else {
            println("LEZ")
        }
        return "result=$j"
    }

    fun name(): String? {
        return this::class.simpleName
    }
}




