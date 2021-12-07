package org.jetbrains

class CommonClass {
    fun callFromThisModule() {
        println("Call from this module")
    }

    fun callFromAnotherModule() {
        println("Call from another module")
    }
}

internal class CommonInternalClass {
    fun function() {
        println("ModuleClass#function call")
    }
}

class AUnused {
    fun functionInUsedClass() {
        println("unused")
    }
}

