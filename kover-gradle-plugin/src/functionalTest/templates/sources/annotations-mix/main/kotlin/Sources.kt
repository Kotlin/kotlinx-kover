package org.jetbrains

annotation class Exclude
annotation class Include

class NotAnnotatedClass {
    fun function() {
        println("function")
    }
}

@Exclude
class ExcludedClass {
    fun function() {
        println("function")
    }
}

@Include
class IncludedClass {
    fun function() {
        println("function")
    }
}

@Include
class ExcludedByName {
    fun function() {
        println("function")
    }
}


@Exclude
@Include
class TogetherClass {
    fun function() {
        println("function")
    }
}

@Include
class MixedClass {
    fun function1() {
        println("function1")
    }

    @Exclude
    fun function2() {
        println("function1")
    }
}



