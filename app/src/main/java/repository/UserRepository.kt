package com.example.helloandroidcristofermunoz.repository

import com.example.helloandroidcristofermunoz.model.User

class UserRepository {
    fun getUsers(): List<User> {
        return listOf(
            User(1, "Karen Acosta", "karen@example.com"),
            User(2, "Emersson", "emersson@example.com"),
            User(3, "Cristofer", "cristofer@example.com")
        )
    }
}