package org.jetbrains

class ExampleClass {
    fun used(value: Int): Int {
        return value + 1
    }

    fun unused(value: Long): Long {
        return value - 1
    }
}

class SecondClass {
    fun anotherUsed(value: Int): Int {
        return value + 1
    }
}

class Unused {
    fun functionInUsedClass() {
        println("unused")
    }
}

