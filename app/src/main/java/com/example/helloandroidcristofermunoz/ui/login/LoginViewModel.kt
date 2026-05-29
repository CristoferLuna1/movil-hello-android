package com.example.helloandroidcristofermunoz.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

        var isValid = true

        if (email.isBlank()) {
            _emailError.value = "Ingrese el correo"
            isValid = false
        } else {
            _emailError.value = null
        }

        if (password.isBlank()) {
            _passwordError.value = "Ingrese la contraseña"
            isValid = false
        } else {
            _passwordError.value = null
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

                    userRepository.logout()

                    val updatedUser = user.copy(isLoggedIn = true)

                    userRepository.updateUser(updatedUser)

                    _isSuccess.value = true

                } else {

                    _errorMessage.value = "Credenciales incorrectas"
                }

            } catch (e: Exception) {

                _errorMessage.value = e.message

            } finally {

                _isLoading.value = false
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