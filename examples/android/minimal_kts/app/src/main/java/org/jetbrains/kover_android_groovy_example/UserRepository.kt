package org.jetbrains.kover_android_groovy_example

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class UserRepository {

    private val _userNameMutableLiveData = MutableLiveData<String>()
    val userNameLiveData: LiveData<String> = _userNameMutableLiveData

    fun setUserName(userName: String) {
        _userNameMutableLiveData.value = userName
    }
}
