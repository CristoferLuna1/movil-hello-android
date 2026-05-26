package com.example.helloandroidcristofermunoz.data.remote.firebase

import com.example.helloandroidcristofermunoz.data.model.Transaction
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val db = FirebaseManager.database

    suspend fun saveTransaction(transaction: Transaction): String {

        val docRef = db.collection("transactions").document()

        val transactionWithId = transaction.copy(firebaseId = docRef.id)

        docRef.set(transactionWithId).await()

        return docRef.id
    }

    suspend fun getTransactions(): List<Transaction> {
        val snapshot = db.collection("transactions").get().await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null

            Transaction(
                    id = 0,
                    firebaseId = doc.id,
                    title = data["title"] as? String ?: "",
                    amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                    category = data["category"] as? String ?: "",
                    type = data["type"] as? String ?: "",
                    date = (data["date"] as? Number)?.toLong() ?: 0L,
                    updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
            )
        }
    }

    suspend fun updateTransaction(transaction: Transaction) {

        val id = transaction.firebaseId ?: return

        db.collection("transactions").document(id).set(transaction).await()
    }

    suspend fun deleteTransaction(transaction: Transaction) {

        val id = transaction.firebaseId ?: return

        db.collection("transactions").document(id).delete().await()
    }
}
