/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package tests.settings.root

import java.lang.AutoCloseable

class RootClass {
    fun action() {
        println("It's root class")
    }
}

class InheritedClass: AutoCloseable {
    override fun close() {
        println("close")
    }
}

annotation class Generated

@Generated
class AnnotatedClass {
    fun function() {
        println("function")
    }
}
