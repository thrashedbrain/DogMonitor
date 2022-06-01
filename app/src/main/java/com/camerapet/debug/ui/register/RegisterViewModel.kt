package com.camerapet.debug.ui.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camerapet.debug.data.repository.UserRepository
import com.camerapet.debug.data.wrappers.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableLiveData<LoginResult>()
    val state: LiveData<LoginResult>
        get() = _state

    fun setData(mail: String, pass: String) {
        userRepository.create(mail, pass) {
            if (it == LoginResult.SUCCESS) {
                viewModelScope.launch {
                    userRepository.createInDb(mail) {
                        _state.value = it
                    }
                }
            } else {
                //TODO Show Err
            }
        }
    }

}