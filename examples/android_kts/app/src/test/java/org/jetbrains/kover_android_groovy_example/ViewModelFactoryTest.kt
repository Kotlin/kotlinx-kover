package org.jetbrains.kover_android_groovy_example

import androidx.lifecycle.ViewModel
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewModelFactoryTest {

    @Test
    fun `verify MainViewModel`() {
        // Given
        val modelClass = MainViewModel::class.java

        // When
        val result = ViewModelFactory.create(modelClass)

        // Then
        assertTrue(result is MainViewModel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `verify unknown ViewModel`() {
        // Given
        val modelClass = object : ViewModel() {}.javaClass

        // When
        ViewModelFactory.create(modelClass)
    }
}