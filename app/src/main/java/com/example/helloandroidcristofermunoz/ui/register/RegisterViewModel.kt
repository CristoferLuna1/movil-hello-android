package com.example.helloandroidcristofermunoz.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.model.User
import com.example.helloandroidcristofermunoz.data.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _nameError = MutableLiveData<String?>()
    val nameError: LiveData<String?> = _nameError

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    private val _confirmPasswordError = MutableLiveData<String?>()
    val confirmPasswordError: LiveData<String?> = _confirmPasswordError

    fun validateAndRegister(name: String, email: String, password: String, confirmPassword: String) {
        _nameError.value = null
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null

        var isValid = true

        if (name.isBlank()) {
            _nameError.value = "El nombre es requerido"
            isValid = false
        } else if (name.length < 3) {
            _nameError.value = "El nombre debe tener al menos 3 caracteres"
            isValid = false
        }

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

        if (confirmPassword.isBlank()) {
            _confirmPasswordError.value = "Confirma tu contraseña"
            isValid = false
        } else if (password != confirmPassword) {
            _confirmPasswordError.value = "Las contraseñas no coinciden"
            isValid = false
        }

        if (!isValid) return

        register(name, email, password)
    }

    private fun register(name: String, email: String, password: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Verificar si el email ya existe
                val allUsers = userRepository.getAllUsers()
                val emailExists = allUsers.any { it.email == email }

                if (emailExists) {
                    _isLoading.value = false
                    _errorMessage.value = "El email ya está registrado"
                    return@launch
                }

                // Crear nuevo usuario
                val user = User(
                    name = name,
                    email = email,
                    password = password,
                    isLoggedIn = false
                )

                userRepository.register(user)

                _isLoading.value = false
                _isSuccess.value = true
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error al registrar usuario"
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
