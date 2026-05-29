package com.example.helloandroidcristofermunoz.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun register(name: String, email: String, password: String): Result<String> {
        return try {

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("No UID")

            val user = hashMapOf(
                "uid" to uid,
                "name" to name,
                "email" to email
            )

            firestore.collection("users")
                .document(uid)
                .set(user)
                .await()

            Result.success(uid)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}