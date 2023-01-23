package org.jetbrains.kover_android_groovy_example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object ViewModelFactory : ViewModelProvider.Factory {

    private val userRepository = UserRepository()

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(userRepository)
        else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    } as T
}