package org.jetbrains

class ExampleClass {
    fun used(value: Int): Int {
        return value + 1
    }

    fun unused(value: Long): Long {
        return value - 1
    }
}

class UnusedClass {
    fun functionInUsedClass() {
        println("unused")
    }
}

