/*
 * Copyright 2017-2024 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
package org.jetbrains.composetests

import androidx.compose.runtime.Composable

@Composable
fun Simple() {
    println("Hello")
}

@Composable
fun WithParam(name: String) {
    println("Hello, " + name)
}

@Composable
fun WithDefParam(name: String = "") {
    println("Hello, def " + name)
}