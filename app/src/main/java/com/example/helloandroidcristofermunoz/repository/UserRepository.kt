package com.example.helloandroidcristofermunoz.repository

import com.example.helloandroidcristofermunoz.model.User

class UserRepository {
    // Lista mutable para simular datos (en app real sería DB o API)
    private val users = mutableListOf(
        User(1, "Karen Acosta", "karen@example.com", 20),
        User(2, "Emersson", "emersson@example.com", 22),
        User(3, "Cristofer", "cristofer@example.com", 21)
    )

    fun getAllUsers(): List<User> {
        return users.toList() // Retorna copia inmutable
    }

    fun addUser(user: User) {
        users.add(user)
    }

    fun deleteUser(userId: Int) {
        users.removeIf { it.id == userId }
    }
}