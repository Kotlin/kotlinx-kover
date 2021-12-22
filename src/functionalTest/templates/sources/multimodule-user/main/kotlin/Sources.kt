package org.jetbrains

internal class UserClass {
    fun function() {
        println("UserClass#function call")
    }
}

class BUnused {
    fun functionInUsedClass() {
        println("unused")
    }
}

