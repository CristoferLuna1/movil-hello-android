package com.example.helloandroidcristofermunoz.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.AppDatabase
import kotlinx.coroutines.launch

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
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val userDao = AppDatabase.getDatabase(context).userDao()
                val currentUser = userDao.getLoggedInUser()
                currentUser?.let {
                    _name.value = it.name
                    _email.value = it.email
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                val userDao = AppDatabase.getDatabase(context).userDao()
                userDao.logoutAll()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateProfileImage(imageUri: String) {
        viewModelScope.launch {
            try {
                val userDao = AppDatabase.getDatabase(context).userDao()
                val currentUser = userDao.getLoggedInUser()
                currentUser?.let {
                    val updatedUser = it.copy(profileImage = imageUri)
                    userDao.update(updatedUser)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun setContext(context: android.content.Context) {
        this.context = context
    }

    private lateinit var context: android.content.Context
}