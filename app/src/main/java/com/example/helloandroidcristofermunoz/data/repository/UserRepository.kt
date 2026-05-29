package com.example.helloandroidcristofermunoz.data.repository

import com.example.helloandroidcristofermunoz.data.dao.UserDao
import com.example.helloandroidcristofermunoz.data.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    suspend fun login(email: String, password: String): User? {
        // Primero cerrar sesión de todos los usuarios
        userDao.logoutAll()
        // Intentar hacer login
        val user = userDao.login(email, password)
        if (user != null) {
            val loggedInUser = user.copy(isLoggedIn = true)
            userDao.update(loggedInUser)
            return loggedInUser
        }
        return null
    }

    suspend fun register(user: User): Long {
        return userDao.insert(user)
    }

    suspend fun logout() {
        userDao.logoutAll()
    }

    suspend fun getLoggedInUser(): User? {
        return userDao.getLoggedInUser()
    }

    suspend fun updateUser(user: User) {
        userDao.update(user)
    }

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }
}
