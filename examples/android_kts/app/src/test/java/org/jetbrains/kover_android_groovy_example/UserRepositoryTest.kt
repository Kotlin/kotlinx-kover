package org.jetbrains.kover_android_groovy_example

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class UserRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val userRepository = UserRepository()

    @Test
    fun `initial case`() {
        // When
        userRepository.userNameLiveData.observeForever {}
        val result = userRepository.userNameLiveData.value

        // Then
        assertNull(result)
    }

    @Test
    fun `setUserName - nominal case`() {
        // Given
        val name = "Foo"
        userRepository.setUserName(name)

        // When
        userRepository.userNameLiveData.observeForever {}
        val result = userRepository.userNameLiveData.value

        // Then
        assertEquals(name, result)
    }
}