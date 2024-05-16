package com.pass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pass.domain.usecase.IsSignedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val isSignedInUseCase: IsSignedInUseCase
) : ViewModel() {

    private val _isSignedInState = MutableStateFlow<Boolean?>(null)
    val isSignedInState: StateFlow<Boolean?> = _isSignedInState

    init {
        viewModelScope.launch {
            isSignedInUseCase().collect { signedIn ->
                _isSignedInState.value = signedIn
            }
        }
    }
}