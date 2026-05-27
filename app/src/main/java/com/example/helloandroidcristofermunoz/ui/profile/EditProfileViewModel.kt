package com.example.helloandroidcristofermunoz.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.model.User
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _currentProfile = MutableLiveData<User?>()
    val currentProfile: LiveData<User?> = _currentProfile

    fun updateProfile(name: String, email: String) {
        when {
            name.isBlank() -> {
                _errorMessage.value = "El nombre es requerido"
                return
            }
            email.isBlank() -> {
                _errorMessage.value = "El email es requerido"
                return
            }
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val userDao = AppDatabase.getDatabase(context).userDao()
                val currentUser = userDao.getLoggedInUser()

                if (currentUser != null) {
                    // Verificar si el email ya existe en otro usuario
                    val existingUser = userDao.getUserByEmail(email)
                    if (existingUser != null && existingUser.id != currentUser.id) {
                        _errorMessage.value = "El email ya está en uso"
                        _isLoading.value = false
                        return@launch
                    }

                    val updatedUser = currentUser.copy(
                        name = name,
                        email = email
                    )
                    userDao.update(updatedUser)
                    _isSuccess.value = true
                } else {
                    _errorMessage.value = "No hay usuario logueado"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar perfil"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCurrentProfile() {
        viewModelScope.launch {
            try {
                val userDao = AppDatabase.getDatabase(context).userDao()
                val currentUser = userDao.getLoggedInUser()
                _currentProfile.value = currentUser
            } catch (e: Exception) {
                // Handle error
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
