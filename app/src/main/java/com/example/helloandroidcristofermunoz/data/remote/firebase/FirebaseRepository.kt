package com.example.helloandroidcristofermunoz.data.remote.firebase

import com.example.helloandroidcristofermunoz.data.model.Transaction
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val db = FirebaseManager.database

    suspend fun saveTransaction(transaction: Transaction) {

        db.collection("transactions")
            .document(transaction.id.toString())
            .set(transaction)
            .await()
    }

    suspend fun getTransactions(): List<Transaction> {

        val snapshot = db.collection("transactions")
            .get()
            .await()

        return snapshot.toObjects(Transaction::class.java)
    }
}