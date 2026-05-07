package com.example.helloandroidcristofermunoz.repository

import com.example.helloandroidcristofermunoz.model.User

class UserRepository {
    // Lista interna mutable para simular persistencia
    private val users = mutableListOf(
        User(1, "Karen Acosta", "karen@example.com", 20),
        User(2, "Emersson", "emersson@example.com", 22),
        User(3, "Cristofer", "cristofer@example.com", 21)
    )

    // Obtener todos los usuarios (copia inmutable)
    fun getAllUsers(): List<User> {
        return users.toList()
    }

    // Obtener usuario por ID
    fun getUserById(id: Int): User? {
        return users.find { it.id == id }
    }

    // Agregar nuevo usuario
    fun addUser(user: User) {
        users.add(user)
    }

    // Actualizar usuario existente
    fun updateUser(user: User) {
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
        }
    }

    // Eliminar usuario
    fun deleteUser(userId: Int) {
        users.removeAll { it.id == userId }
    }
}