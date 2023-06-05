package org.jetbrains.kover.test

class ExampleClass(
    val a: String,
    var b: String
) {

    val i: Int = intFun()
    val j: Int = intFun()

    init {
        println("AAA")
    }

    fun fun1() {
        println("fun1")
    }

    fun fun2() {
        println("fun2")
    }

    private fun intFun(): Int {
        return 10
    }

}