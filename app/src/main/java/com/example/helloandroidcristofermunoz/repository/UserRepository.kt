package com.example.helloandroidcristofermunoz.repository

import com.example.helloandroidcristofermunoz.model.User

class UserRepository {
    fun getUsers(): List<User> {
        return listOf(
            User(1, "Karen Acosta", "karen@example.com", 20),
            User(2, "Emersson", "emersson@example.com", 22),
            User(3, "Cristofer", "cristofer@example.com", 21)
        )
    }
}