package org.jetbrains

class MyBranchedClass {
    fun foo(value: Int) {
        if (value < 0) {
            println("LE")
        } else if (value == 0) {
            println("EQ")
        } else {
            println("GE")
        }
    }
}
