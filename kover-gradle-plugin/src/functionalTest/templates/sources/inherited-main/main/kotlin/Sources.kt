package org.jetbrains

import java.io.Closeable
import java.lang.Cloneable

// inheritable types
interface Interface

open class A


// children
class RegularClass {
    fun function() {
        println("function")
    }
}

// direct inheritance of project class
open class B: A() {
    fun functionB() {
        println("function")
    }
}

// direct implementation of project interface
open class C : Interface {
    fun functionC() {
        println("function")
    }
}

// direct implementation of non-project interface
open class D: AutoCloseable {
    fun functionD() {
        println("function")
    }

    override fun close() {
        println("foo")
    }
}

// direct inheritance of project class
class AChild: A() {
    fun functionAA() {
        println("function")
    }
}

@MyAnnotation
// indirect inheritance of project class
class BChild: B() {
    fun functionBB() {
        println("function")
    }
}

// indirect implementation of project interface
class CChild: C() {
    fun functionCC() {
        println("function")
    }
}

// indirect implementation of non-project interface
class DChild: D() {
    fun functionDD() {
        println("function")
    }
}

// indirect implementation of twice indirect non-project interface (AutoCloseable)
class CloseableClass : Closeable {
    fun functionCC() {
        println("function")
    }

    override fun close() {
        println("foo")
    }
}

annotation class MyAnnotation
