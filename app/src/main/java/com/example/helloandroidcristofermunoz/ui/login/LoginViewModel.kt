package com.example.helloandroidcristofermunoz.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.model.User
import com.example.helloandroidcristofermunoz.data.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    fun validateAndLogin(email: String, password: String) {
        _emailError.value = null
        _passwordError.value = null

        var isValid = true

        if (email.isBlank()) {
            _emailError.value = "El email es requerido"
            isValid = false
        } else if (!email.contains("@")) {
            _emailError.value = "Email inválido"
            isValid = false
        }

        if (password.isBlank()) {
            _passwordError.value = "La contraseña es requerida"
            isValid = false
        } else if (password.length < 6) {
            _passwordError.value = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        }

        if (!isValid) return

        login(email, password)
    }

    private fun login(email: String, password: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val user = userRepository.login(email, password)
                if (user != null) {
                    _isLoading.value = false
                    _isSuccess.value = true
                } else {
                    _isLoading.value = false
                    _errorMessage.value = "Credenciales incorrectas"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error al iniciar sesión"
            }
        }
    }

    fun resetSuccessState() {
        _isSuccess.value = false
    }

    fun resetErrorState() {
        _errorMessage.value = null
    }
}
