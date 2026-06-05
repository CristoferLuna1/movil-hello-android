package com.example.helloandroidcristofermunoz.data.repository

import android.util.Log
import com.example.helloandroidcristofermunoz.data.dao.SavingsPlanDao
import com.example.helloandroidcristofermunoz.data.dao.TransactionDao
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.remote.firebase.FirebaseRepository
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
        private val transactionDao: TransactionDao,
        private val savingsPlanDao: SavingsPlanDao
) {

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

    fun getTransactionsByUser(userId: Int): Flow<List<Transaction>> =
            transactionDao.getTransactionsByUser(userId)

    // INSERT + FIREBASE
    suspend fun insert(transaction: Transaction) {

        Log.d("OFFLINE_CRISTOFER", "1 - Entró a insert()")

        try {

            Log.d("OFFLINE_CRISTOFER", "2 - Antes de saveTransaction()")

            val firebaseId = firebaseRepository.saveTransaction(transaction)

            Log.d("OFFLINE_CRISTOFER", "3 - Firebase respondió")

            transactionDao.insert(
                    transaction.copy(
                            firebaseId = firebaseId,
                            pendingSync = false,
                            updatedAt = System.currentTimeMillis()
                    )
            )

            Log.d("OFFLINE_CRISTOFER", "4 - Guardó en Room")
        } catch (e: Exception) {

            Log.e("OFFLINE_CRISTOFER", "ERROR EN FIREBASE", e)

            transactionDao.insert(
                    transaction.copy(pendingSync = true, updatedAt = System.currentTimeMillis())
            )

            Log.d("OFFLINE_CRISTOFER", "5 - Guardó offline")
        }
    }

    suspend fun syncTransactions() {

        if (isSyncing) {
            Log.d("SYNCCristofer", "⚠️ Sync BLOQUEADO")
            return
        }

        isSyncing = true

        try {

            Log.d("SYNCCristofer", "🔥 INICIO SYNC")

            // ==========================
            // SUBIR PENDIENTES OFFLINE
            // ==========================

            val pending = transactionDao.getPendingTransactions()

            Log.d("SYNCCristofer", "📤 Pendientes = ${pending.size}")

            pending.forEach { transaction ->
                try {

                    val firebaseId = firebaseRepository.saveTransaction(transaction)

                    transactionDao.update(
                            transaction.copy(
                                    firebaseId = firebaseId,
                                    pendingSync = false,
                                    updatedAt = System.currentTimeMillis()
                            )
                    )

                    Log.d("SYNCCristofer", "✅ Pendiente sincronizada")
                } catch (e: Exception) {

                    Log.e("SYNCCristofer", "❌ Error sincronizando pendiente", e)
                }
            }

            // ==========================
            // DESCARGAR FIREBASE
            // ==========================

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

                    if (remoteTx.updatedAt > existing.updatedAt) {

                        Log.d("SYNCCristofer", "♻️ ACTUALIZANDO: ${remoteTx.firebaseId}")

                        transactionDao.update(remoteTx.copy(id = existing.id))
                    }
                }
            }

            Log.d("SYNCCristofer", "✅ FIN SYNC")
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

    suspend fun deleteTransaction(transaction: Transaction) {

        transactionDao.delete(transaction)

        try {

            firebaseRepository.deleteTransaction(transaction)
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun decrementSavingsInstallments(userId: Int, monthYear: Int, amount: Double) {

        val currentPlan = savingsPlanDao.getActiveSavingsPlanForMonth(userId, monthYear)

        currentPlan?.let {
            if (it.paidInstallments > 0) {

                val updatedPlan =
                        it.copy(
                                currentSaved = maxOf(0.0, it.currentSaved - amount),
                                paidInstallments = it.paidInstallments - 1
                        )

                savingsPlanDao.update(updatedPlan)
            }
        }
    }
}
