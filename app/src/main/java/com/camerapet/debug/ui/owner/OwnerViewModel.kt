package com.camerapet.debug.ui.owner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camerapet.debug.data.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {
    private val _tags = MutableLiveData<String>()
    val tags: LiveData<String>
        get() = _tags

    init {
        viewModelScope.launch {
            userRepository.updateUserTag()
            userRepository.getUserTag {
                _tags.value = it
            }
        }
    }

    fun updateWatchingState() {
        userRepository.updateUserState("watching")
    }

    fun updateState() {
        userRepository.updateUserState("idle")
    }

}