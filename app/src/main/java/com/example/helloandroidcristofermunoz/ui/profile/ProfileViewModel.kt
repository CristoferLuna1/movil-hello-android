package com.example.helloandroidcristofermunoz.ui.profile

import androidx.lifecycle.*
import com.example.helloandroidcristofermunoz.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    private val _isLoggingOut = MutableLiveData<Boolean>()
    val isLoggingOut: LiveData<Boolean> = _isLoggingOut

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val user = userRepository.getLoggedInUser()

                if (user != null) {
                    _name.value = user.name
                    _email.value = user.email
                    _isLoggedIn.value = true
                } else {
                    _isLoggedIn.value = false
                    _name.value = "Invitado"
                    _email.value = "No ha iniciado sesión"
                }
            } catch (e: Exception) {
                _isLoggedIn.value = false
                _name.value = "Error"
                _email.value = "Error al cargar perfil"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoggingOut.value = true

            try {
                userRepository.logout()

                _isLoggedIn.value = false
                _name.value = "Invitado"
                _email.value = "No ha iniciado sesión"
            } finally {
                _isLoggingOut.value = false
            }
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }
    fun updateProfileImage(url: String) {
        viewModelScope.launch {
            val user = userRepository.getLoggedInUser()
            if (user != null) {
                userRepository.updateUser(user.copy(profileImage = url))
            }
        }
    }
}
