package com.example.helloandroidcristofermunoz.data.remote.firebase

import com.example.helloandroidcristofermunoz.data.model.Transaction
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val db = FirebaseManager.database

    suspend fun saveTransaction(transaction: Transaction): String {

        val docRef = db.collection("transactions").document()

        val transactionWithId =
            transaction.copy(firebaseId = docRef.id)

        docRef.set(transactionWithId).await()

        return docRef.id
    }

    suspend fun getTransactions(): List<Transaction> {

        val snapshot =
            db.collection("transactions")
                .get()
                .await()

        return snapshot.documents.mapNotNull { doc ->

            val data = doc.data ?: return@mapNotNull null

            Transaction(

                id = 0,

                firebaseId = doc.id,

                title =
                    data["title"] as? String ?: "",

                amount =
                    (data["amount"] as? Number)?.toDouble() ?: 0.0,

                category =
                    data["category"] as? String ?: "",

                customCategory =
                    data["customCategory"] as? String,

                type =
                    data["type"] as? String ?: "",

                date =
                    (data["date"] as? Number)?.toLong() ?: 0L,

                paymentDay =
                    (data["paymentDay"] as? Number)?.toInt(),

                endDate =
                    (data["endDate"] as? Number)?.toLong(),

                isMonthlyPersistent =
                    data["isMonthlyPersistent"] as? Boolean ?: false,

                monthYear =
                    (data["monthYear"] as? Number)?.toInt() ?: 0,

                userId =
                    (data["userId"] as? Number)?.toInt() ?: 0,

                updatedAt =
                    (data["updatedAt"] as? Number)?.toLong()
                        ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun updateTransaction(transaction: Transaction) {

        if (transaction.firebaseId.isBlank()) return

        db.collection("transactions")
            .document(transaction.firebaseId)
            .set(transaction)
            .await()
    }

    suspend fun deleteTransaction(transaction: Transaction) {

        if (transaction.firebaseId.isBlank()) return

        db.collection("transactions")
            .document(transaction.firebaseId)
            .delete()
            .await()
    }
}