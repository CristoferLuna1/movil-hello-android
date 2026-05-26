package com.example.helloandroidcristofermunoz.data.repository

import android.util.Log
import com.example.helloandroidcristofermunoz.data.dao.TransactionDao
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.remote.firebase.FirebaseRepository
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    private val firebaseRepository = FirebaseRepository()
    companion object {
        private var isSyncing = false
    }

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun getTotalBalance(): Double = transactionDao.getTotalBalance() ?: 0.0
    suspend fun getTotalIncome(): Double = transactionDao.getTotalIncome() ?: 0.0
    suspend fun getTotalExpenses(): Double = transactionDao.getTotalExpenses() ?: 0.0

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
            transactionDao.getTransactionsByDateRange(startDate, endDate)

    fun searchTransactions(searchQuery: String): Flow<List<Transaction>> =
            transactionDao.searchTransactions(searchQuery)

    // 🔥 INSERT CORREGIDO (Firebase + Room bien sincronizado)
    suspend fun insert(transaction: Transaction) {

        val firebaseId =
                if (transaction.firebaseId.isNullOrBlank()) {
                    firebaseRepository.saveTransaction(transaction)
                } else {
                    transaction.firebaseId
                }

        val newTransaction =
                transaction.copy(firebaseId = firebaseId, updatedAt = System.currentTimeMillis())

        transactionDao.insert(newTransaction)
    }

    suspend fun syncTransactions() {

        if (isSyncing) {
            Log.d("SYNCCristofer", "⚠️ Sync BLOQUEADO")
            return
        }

        isSyncing = true

        try {

            Log.d("SYNCCristofer", "🔥 INICIO SYNC")

            val remote = firebaseRepository.getTransactions()
            val local = transactionDao.getAllOnce()

            Log.d("SYNCCristofer", "📡 Remote size = ${remote.size}")
            Log.d("SYNCCristofer", "💾 Local size = ${local.size}")

            val localMap = local.associateBy { it.firebaseId }

            remote.forEach { remoteTx ->
                val existing = localMap[remoteTx.firebaseId]

                if (existing == null) {

                    Log.d("SYNCCristofer", "🆕 INSERTANDO EN ROOM: ${remoteTx.firebaseId}")

                    transactionDao.insert(remoteTx)
                } else {

                    Log.d("SYNCCristofer", "♻️ YA EXISTE: ${remoteTx.firebaseId}")

                    if (remoteTx.updatedAt > existing.updatedAt) {

                        transactionDao.update(remoteTx.copy(id = existing.id))
                    }
                }
            }

            Log.d("SYNCCristofer", "✅ FIN SYNC2")
        } catch (e: Exception) {

            Log.e("SYNCCristofer", "❌ ERROR", e)
        } finally {

            isSyncing = false
        }
    }
    suspend fun update(transaction: Transaction) {

        val updated = transaction.copy(updatedAt = System.currentTimeMillis())

        transactionDao.update(updated)

        try {
            firebaseRepository.updateTransaction(updated)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun delete(transaction: Transaction) {

        transactionDao.delete(transaction)

        try {
            firebaseRepository.deleteTransaction(transaction)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)
    }
}
