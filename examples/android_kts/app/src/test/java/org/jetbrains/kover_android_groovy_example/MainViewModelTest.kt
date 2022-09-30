package org.jetbrains.kover_android_groovy_example

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val userNameMutableLiveData = MutableLiveData("Nino")

    private val userRepository: UserRepository = mockk {
        every { userNameLiveData } returns userNameMutableLiveData
    }

    private val mainViewModel = MainViewModel(userRepository)

    @Test
    fun `nominal case`() {
        // When
        mainViewModel.messageLiveData.observeForever {}
        val result = mainViewModel.messageLiveData.value

        // Then
        assertEquals("Hello, Nino!", result)
    }

    @Test
    fun `verify onUserNameChanged`() {
        // Given
        val name = "Foo"
        justRun { userRepository.setUserName(any()) }

        // When
        mainViewModel.onUserNameChanged(name)

        // Then
        verify(exactly = 1) {
            userRepository.userNameLiveData
            userRepository.setUserName(name)
        }
        confirmVerified(userRepository)
    }

    @Test
    fun `verify onUserNameChanged with null param`() {
        // Given
        justRun { userRepository.setUserName(any()) }

        // When
        mainViewModel.onUserNameChanged(null)

        // Then
        verify(exactly = 1) { userRepository.userNameLiveData }
        confirmVerified(userRepository)
    }
}