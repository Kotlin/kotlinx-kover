package org.jetbrains

class ClassWithInline {
    inline fun main(block: () -> Unit) {
        block()
        println("first")
        println("second")
    }
}

class TestingClass {
    fun test() {
        ClassWithInline().main { println("") }
    }
}

