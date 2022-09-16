package org.jetbrains

sealed class Sealed
data class SealedChild(val a: Int): Sealed()

sealed class SealedWithInit {
    init {
        println("SealedWithInit")
    }
}
data class SealedWithInitChild(val a: Int): SealedWithInit()

sealed class SealedWithConstructor {
    constructor() {
        println("SealedWithConstructor")
    }
}
data class SealedWithConstructorChild(val a: Int): SealedWithConstructor()
