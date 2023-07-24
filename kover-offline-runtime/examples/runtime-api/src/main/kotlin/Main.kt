package org.jetbrains.kotlinx.kover

class MainClass {
    var state: Int = 0;
    fun readState(): DataClass {
        return DataClass(10);
    }

    fun incrementState() {
        state++
    }

}

class UnusedClass {
    fun printHello() {
        println("Hello World!")
    }
}
