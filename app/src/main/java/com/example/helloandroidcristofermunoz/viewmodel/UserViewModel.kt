package com.example.helloandroidcristofermunoz.viewmodel

import androidx.lifecycle.ViewModel
import com.example.helloandroidcristofermunoz.repository.UserRepository

class UserViewModel : ViewModel() {

    // Instancia del repositorio para acceder a datos
    private val repository = UserRepository()

}