package org.jetbrains.kover_android_groovy_example

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {

    val messageLiveData = Transformations.map(userRepository.userNameLiveData) {
        "Hello, $it!"
    }

    fun onUserNameChanged(userName: String?) {
        if (userName != null) {
            userRepository.setUserName(userName)
        }
    }
}