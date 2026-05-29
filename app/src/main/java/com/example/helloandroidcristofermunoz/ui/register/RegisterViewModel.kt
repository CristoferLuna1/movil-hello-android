package com.example.helloandroidcristofermunoz.ui.register

import androidx.lifecycle.*
import com.example.helloandroidcristofermunoz.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    val isLoading = MutableLiveData(false)
    val isSuccess = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>()

    val nameError = MutableLiveData<String?>()
    val emailError = MutableLiveData<String?>()
    val passwordError = MutableLiveData<String?>()
    val confirmPasswordError = MutableLiveData<String?>()

    fun validateAndRegister(name: String, email: String, password: String, confirm: String) {

        var valid = true

        if (name.isBlank()) {
            nameError.value = "Nombre vacío"
            valid = false
        } else nameError.value = null

        if (email.isBlank()) {
            emailError.value = "Email vacío"
            valid = false
        } else emailError.value = null

        if (password.length < 6) {
            passwordError.value = "Mínimo 6 caracteres"
            valid = false
        } else passwordError.value = null

        if (password != confirm) {
            confirmPasswordError.value = "No coincide"
            valid = false
        } else confirmPasswordError.value = null

        if (!valid) return

        viewModelScope.launch {
            isLoading.value = true

            val result = repository.register(name, email, password)

            if (result.isSuccess) {
                isSuccess.value = true
            } else {
                errorMessage.value = result.exceptionOrNull()?.message
            }

            isLoading.value = false
        }
    }

    fun resetSuccessState() {
        isSuccess.value = false
    }

    fun resetErrorState() {
        errorMessage.value = null
    }
}