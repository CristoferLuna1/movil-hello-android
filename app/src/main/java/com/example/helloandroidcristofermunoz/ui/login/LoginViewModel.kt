package com.example.helloandroidcristofermunoz.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.AppDatabase
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun login(email: String, password: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val userDao = AppDatabase.getDatabase(context).userDao()
                val user = userDao.login(email, password)

                if (user != null) {
                    // Establecer isLoggedIn = true para el usuario que hace login
                    userDao.logoutAll()
                    val updatedUser = user.copy(isLoggedIn = true)
                    userDao.update(updatedUser)
                    _isSuccess.value = true
                } else {
                    _errorMessage.value = "Credenciales incorrectas"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al iniciar sesión"
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

    private lateinit var context: android.content.Context
}
