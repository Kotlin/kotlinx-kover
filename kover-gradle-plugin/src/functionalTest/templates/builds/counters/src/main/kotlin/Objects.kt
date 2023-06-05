package org.jetbrains

object UnusedObject

object UnusedObjectWithInit {
    init {
        println("hello")
    }
}

object UnusedObjectWithVal {
    val x = 123
}

object UnusedObjectWithFun {
    fun hello() {
        println("Hello")
    }
}

object UnusedObjectFun {
    init {
        println("hello")
    }
    val x = 123
    fun hello() {
        println("Hello")
    }
}


object UsedObject

object UsedObjectWithInit {
    init {
        println("hello")
    }
}

object UsedObjectWithVal {
    val x = 123
}

object UsedObjectWithFun {
    fun hello() {
        println("Hello")
    }
}

object UsedObjectFun {
    init {
        println("hello")
    }
    val x = 123
    fun hello() {
        println("Hello")
    }
}
