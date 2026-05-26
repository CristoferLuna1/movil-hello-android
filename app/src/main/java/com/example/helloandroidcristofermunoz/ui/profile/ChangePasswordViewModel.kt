package com.example.helloandroidcristofermunoz.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.AppDatabase
import kotlinx.coroutines.launch

class ChangePasswordViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        when {
            currentPassword.isBlank() -> {
                _errorMessage.value = "La contraseña actual es requerida"
                return
            }
            newPassword.isBlank() -> {
                _errorMessage.value = "La nueva contraseña es requerida"
                return
            }
            newPassword.length < 6 -> {
                _errorMessage.value = "La nueva contraseña debe tener al menos 6 caracteres"
                return
            }
            confirmPassword.isBlank() -> {
                _errorMessage.value = "Confirmar la nueva contraseña"
                return
            }
            newPassword != confirmPassword -> {
                _errorMessage.value = "Las contraseñas no coinciden"
                return
            }
            currentPassword == newPassword -> {
                _errorMessage.value = "La nueva contraseña debe ser diferente a la actual"
                return
            }
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val userDao = AppDatabase.getDatabase(context).userDao()
                val currentUser = userDao.getLoggedInUser()

                if (currentUser != null) {
                    // Verificar que la contraseña actual sea correcta
                    if (currentUser.password != currentPassword) {
                        _errorMessage.value = "La contraseña actual es incorrecta"
                        _isLoading.value = false
                        return@launch
                    }

                    val updatedUser = currentUser.copy(password = newPassword)
                    userDao.update(updatedUser)
                    _isSuccess.value = true
                } else {
                    _errorMessage.value = "No hay usuario logueado"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cambiar contraseña"
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
