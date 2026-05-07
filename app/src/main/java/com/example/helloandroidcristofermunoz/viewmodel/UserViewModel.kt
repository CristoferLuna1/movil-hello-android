package com.example.helloandroidcristofermunoz.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.helloandroidcristofermunoz.model.User
import com.example.helloandroidcristofermunoz.repository.UserRepository

class UserViewModel : ViewModel() {

    // Instancia del repositorio para acceder a datos
    private val repository = UserRepository()

    // LiveData para la lista de usuarios (la UI observa esto)
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    // LiveData para el usuario seleccionado (para navegación a detalle)
    private val _selectedUser = MutableLiveData<User?>()
    val selectedUser: LiveData<User?> = _selectedUser

    // LiveData para estado de carga (ProgressBar)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadUsers()
    }

    fun loadUsers() {
        _isLoading.value = true

        // Simular retardo de carga (como si fuera una llamada de red)
        Handler(Looper.getMainLooper()).postDelayed({
            _users.value = repository.getAllUsers()
            _isLoading.value = false
        }, 500)
    }

    fun selectUser(user: User) {
        _selectedUser.value = user
    }

    fun addUser(name: String, email: String, age: Int) {
        val newId = (repository.getAllUsers().maxOfOrNull { it.id } ?: 0) + 1
        val newUser = User(newId, name, email, age)
        repository.addUser(newUser)
        loadUsers() // Refrescar lista
    }

    fun deleteUser(userId: Int) {
        repository.deleteUser(userId)
        loadUsers() // Refrescar lista
    }

}