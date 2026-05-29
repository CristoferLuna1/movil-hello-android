package com.example.helloandroidcristofermunoz.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

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

        val user = auth.currentUser

        if (user != null) {

            _isLoggedIn.value = true

            _name.value =
                user.displayName ?: "Usuario"

            _email.value =
                user.email ?: "Sin email"

        } else {

            _isLoggedIn.value = false

            _name.value = "Invitado"

            _email.value = "No ha iniciado sesión"
        }
    }

    fun logout() {

        _isLoggingOut.value = true

        auth.signOut()

        _isLoggedIn.value = false
        _name.value = "Invitado"
        _email.value = "No ha iniciado sesión"

        _isLoggingOut.value = false
    }

    fun refreshProfile() {
        loadUserProfile()
    }

    fun updateProfileImage(url: String) {
        // Luego lo conectamos con Firebase Storage
    }
}