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

    init {
        loadUsers()
    }

    fun loadUsers() {
        _users.value = repository.getUsers()
    }

}