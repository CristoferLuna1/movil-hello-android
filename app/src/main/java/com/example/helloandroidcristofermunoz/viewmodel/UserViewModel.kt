package com.example.helloandroidcristofermunoz.viewmodel

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

    init {
        loadUsers()
    }

    fun loadUsers() {
        _users.value = repository.getUsers()
    }

    // Lista local para simular CRUD (ya que repository no tiene persistencia)
    private val localUsers = mutableListOf<User>().also { it.addAll(repository.getUsers()) }

    fun selectUser(user: User) {
        _selectedUser.value = user
    }

    fun addUser(name: String, email: String, age: Int) {
        val newId = (localUsers.maxOfOrNull { it.id } ?: 0) + 1
        val newUser = User(newId, name, email, age)
        localUsers.add(newUser)
        _users.value = localUsers.toList()
    }

    fun deleteUser(userId: Int) {
        localUsers.removeAll { it.id == userId }
        _users.value = localUsers.toList()
    }

}