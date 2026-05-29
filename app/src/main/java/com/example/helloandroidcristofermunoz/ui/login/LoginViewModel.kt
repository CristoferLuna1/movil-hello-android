package com.example.helloandroidcristofermunoz.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.repository.UserRepository
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.example.helloandroidcristofermunoz.data.repository.AuthRepository

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

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
        Log.d("AUTH_FLOW", "VALIDATE AND LOGIN -> email=$email password=$password")
        var isValid = true
        Log.d(
                "AUTH_FLOW",
                "VALIDATE FIELDS -> email isBlank=${email.isBlank()} password isBlank=${password.isBlank()}"
        )

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

        Log.d("AUTH_FLOW", "START LOGIN PROCESS -> email=$email")

        _isLoading.value = true

        viewModelScope.launch {
            try {

                val result = authRepository.login(email, password)

                if (result.isSuccess) {

                    Log.d("AUTH_FLOW", "LOGIN SUCCESS")

                    _isSuccess.value = true
                } else {

                    Log.e("AUTH_FLOW", "LOGIN FAILED -> ${result.exceptionOrNull()?.message}")

                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Error de login"
                }
            } catch (e: Exception) {

                Log.e("AUTH_FLOW", "LOGIN EXCEPTION -> ${e.message}")

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
