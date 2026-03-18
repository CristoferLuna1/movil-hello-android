package com.apellido.helloandroid.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apellido.helloandroid.model.User
import com.apellido.helloandroid.repository.UserRepository

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
    
 ¿
