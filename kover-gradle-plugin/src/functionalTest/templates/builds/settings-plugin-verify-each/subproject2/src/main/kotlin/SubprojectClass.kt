/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package tests.settings.subproject2

class Subproject2Class {
    fun action() {
        println("It's class from the subproject2")
    }
}

class ToRegularSubprojectClass {
    fun action() {
        println("It's class for the subproject")
    }
}

class IgnoredClass {
    fun action() {
        println("It's uncovered class")
    }
}