package com.example.helloandroidcristofermunoz.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "AUTH_FLOW"
    }

    suspend fun register(name: String, email: String, password: String): Result<String> {

        return try {

            Log.d(TAG, "REGISTER START -> email=$email")

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("No UID")

            Log.d(TAG, "REGISTER SUCCESS -> uid=$uid")

            val user = hashMapOf(
                "uid" to uid,
                "name" to name,
                "email" to email
            )

            firestore.collection("users")
                .document(uid)
                .set(user)
                .await()

            Log.d(TAG, "FIRESTORE USER SAVED")

            Result.success(uid)

        } catch (e: Exception) {

            Log.e(TAG, "REGISTER FAILED -> ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<String> {

        return try {

            Log.d(TAG, "LOGIN START -> email=$email password=$password")

            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid

            Log.d(TAG, "FIREBASE RESPONSE USER = $uid")

            if (uid != null) {
                Log.d(TAG, "LOGIN SUCCESS -> uid=$uid")
                Result.success(uid)
            } else {
                Log.e(TAG, "LOGIN FAILED -> user null")
                Result.failure(Exception("User null"))
            }

        } catch (e: Exception) {

            Log.e(TAG, "LOGIN ERROR -> ${e.message}", e)
            Result.failure(e)
        }
    }

    fun logout() {
        Log.d(TAG, "LOGOUT")
        auth.signOut()
    }

    fun getCurrentUser(): String? {
        val uid = auth.currentUser?.uid
        Log.d(TAG, "GET CURRENT USER -> $uid")
        return uid
    }
}   