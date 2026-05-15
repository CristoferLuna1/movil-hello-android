package com.example.helloandroidcristofermunoz.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _name =
        MutableLiveData<String>()

    val name: LiveData<String>
        get() = _name

    private val _email =
        MutableLiveData<String>()

    val email: LiveData<String>
        get() = _email

    init {

        _name.value = "Juan Pérez"

        _email.value = "juan@email.com"
    }
}