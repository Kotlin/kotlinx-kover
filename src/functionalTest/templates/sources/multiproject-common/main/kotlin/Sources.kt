package org.jetbrains

class CommonClass {
    fun callFromThisModule() {
        println("Call from this project")
    }

    fun callFromAnotherModule() {
        println("Call from another project")
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

