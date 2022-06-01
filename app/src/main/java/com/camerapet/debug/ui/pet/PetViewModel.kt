package com.camerapet.debug.ui.pet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camerapet.debug.data.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {
    private val _tags = MutableLiveData<String>()
    val tags: LiveData<String>
        get() = _tags

    private val _state = MutableLiveData<String>()
    val state: LiveData<String>
        get() = _state

    init {
        viewModelScope.launch {
            userRepository.updateUserTag()
            userRepository.getUserTag {
                _tags.value = it
            }
            userRepository.getStateTag("state", viewModelScope).collectLatest {
                _state.value = it
            }
        }
    }
}