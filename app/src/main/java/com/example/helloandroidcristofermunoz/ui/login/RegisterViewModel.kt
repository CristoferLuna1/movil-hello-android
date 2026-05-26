package com.example.helloandroidcristofermunoz.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.model.User
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        when {
            name.isBlank() -> {
                _errorMessage.value = "El nombre es requerido"
                return
            }
            email.isBlank() -> {
                _errorMessage.value = "El email es requerido"
                return
            }
            password.isBlank() -> {
                _errorMessage.value = "La contraseña es requerida"
                return
            }
            password.length < 6 -> {
                _errorMessage.value = "La contraseña debe tener al menos 6 caracteres"
                return
            }
            password != confirmPassword -> {
                _errorMessage.value = "Las contraseñas no coinciden"
                return
            }
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val userDao = AppDatabase.getDatabase(context).userDao()
                
                // Verificar si el email ya existe
                val existingUser = userDao.getUserByEmail(email)
                if (existingUser != null) {
                    _errorMessage.value = "El email ya está registrado"
                    _isLoading.value = false
                    return@launch
                }

                // Crear nuevo usuario
                val user = User(
                    name = name,
                    email = email,
                    password = password,
                    isLoggedIn = true
                )
                
                userDao.insert(user)
                _isSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Error al registrarse"
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

    fun setContext(context: android.content.Context) {
        this.context = context
    }

    private lateinit var context: android.content.Context
}
