package org.jetbrains

annotation class Exclude
annotation class ExcludeByMask
annotation class OverriddenExclude

class NotExcludedClass {
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


class PartiallyExcludedClass {
    @OverriddenExclude
    fun function1() {
        println("function1")
    }

    @ExcludeByMask
    fun function2() {
        println("function2")
    }

    @ExcludeByMask
    inline fun inlined() {
        println("inlined")
    }
}

@Exclude
inline fun inlinedExcluded() {
    inlinedNotExcluded()
    notExcluded()
}

inline fun inlinedNotExcluded() {
    println("inlinedExcluded")
}

fun notExcluded() {
    println("inlinedExcluded")
}


