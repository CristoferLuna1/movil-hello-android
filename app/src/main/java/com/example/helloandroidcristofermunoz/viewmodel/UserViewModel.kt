package com.example.helloandroidcristofermunoz.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.helloandroidcristofermunoz.model.User
import com.example.helloandroidcristofermunoz.repository.UserRepository

class UserViewModel : ViewModel() {
    
    // Instancia del repositorio
    private val repository = UserRepository()
    
    // LiveData privado mutable (solo el ViewModel puede modificarlo)
    private val _users = MutableLiveData<List<User>>()
    // LiveData público inmutable (la View solo puede observarlo)
    val users: LiveData<List<User>> = _users
    
    // Usuario seleccionado (para navegación)
    private val _selectedUser = MutableLiveData<User?>()
    val selectedUser: LiveData<User?> = _selectedUser
    
    // Estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        // Cargar usuarios al inicializar el ViewModel
        loadUsers()
    }


    // Cargar lista de usuarios
    fun loadUsers() {
        _isLoading.value = true
    }

    // Simular delay de red (en app real sería una llamada suspend)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            _users.value = repository.getAllUsers()
            _isLoading.value = false
        }, 500) // 500ms de delay
    }
    
    // Seleccionar un usuario
    fun selectUser(user: User) {
        _selectedUser.value = user
    }
    
    // Agregar nuevo usuario
    fun addUser(name: String, email: String, age: Int) {
        val newId = (repository.getAllUsers().maxOfOrNull { it.id } ?: 0) + 1
        val newUser = User(newId, name, email, age)
        repository.addUser(newUser)
        loadUsers() // Recargar lista
    }
    
    // Eliminar usuario
    fun deleteUser(userId: Int) {
        repository.deleteUser(userId)
        loadUsers() // Recargar lista
    }
}

    

