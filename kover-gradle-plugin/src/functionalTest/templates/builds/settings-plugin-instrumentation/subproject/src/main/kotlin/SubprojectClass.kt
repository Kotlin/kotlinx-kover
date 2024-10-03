/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package tests.settings.subproject

class SubprojectClass {
    fun action() {
        println("It's class from the subproject")
    }
}

class Tested {
    fun action() {
        println("It's tested subproject class")
    }
}
