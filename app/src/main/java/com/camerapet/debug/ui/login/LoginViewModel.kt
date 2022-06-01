package com.camerapet.debug.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.camerapet.debug.data.repository.UserRepository
import com.camerapet.debug.data.wrappers.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableLiveData<LoginResult>()
    val state: LiveData<LoginResult>
        get() = _state

    fun login(mail: String, pass: String) {
        userRepository.login(mail, pass) {
            _state.value = it
        }
    }
}