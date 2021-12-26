package org.jetbrains

class CommonClass {
    fun callFromThisProject() {
        println("Call from this project")
    }

    fun callFromAnotherProject() {
        println("Call from another project")
    }
}

internal class CommonInternalClass {
    fun function() {
        println("CommonInternalClass#function call")
    }
}

class AUnused {
    fun functionInUsedClass() {
        println("unused")
    }
}

