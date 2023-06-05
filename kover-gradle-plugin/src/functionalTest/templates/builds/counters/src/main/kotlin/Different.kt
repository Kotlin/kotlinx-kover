package org.jetbrains

class Different {
    fun emptyFun() {
        // no-op
    }

    fun helloWorld() {
        println("Hello World!")
    }

    @Deprecated("This function should NOT be included in the report", level = DeprecationLevel.ERROR)
    fun deprecatedError() {
        println("deprecated")
    }

    @Deprecated("This function should NOT be included in the report", level = DeprecationLevel.HIDDEN)
    fun deprecatedHidden() {
        println("deprecated")
    }

    @Deprecated("This function should be included in the report", level = DeprecationLevel.WARNING)
    fun deprecatedWarn() {
        println("deprecated")
    }
}
