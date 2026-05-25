package com.example.helloandroidcristofermunoz.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.model.User
import com.example.helloandroidcristofermunoz.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

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

    private fun loadUserProfile() {
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
        _isLoggingOut.value = true
        viewModelScope.launch {
            try {
                userRepository.logout()
                _isLoggingOut.value = false
                _isLoggedIn.value = false
                _name.value = "Invitado"
                _email.value = "No ha iniciado sesión"
            } catch (e: Exception) {
                _isLoggingOut.value = false
            }
        }
    }

    fun refreshProfile() {
        loadUserProfile()
    }
}
