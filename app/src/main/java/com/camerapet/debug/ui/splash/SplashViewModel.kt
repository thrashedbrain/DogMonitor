package com.camerapet.debug.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.camerapet.debug.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    fun checkUser() = liveData {
        emit(userRepository.checkUserAuth())
    }
}