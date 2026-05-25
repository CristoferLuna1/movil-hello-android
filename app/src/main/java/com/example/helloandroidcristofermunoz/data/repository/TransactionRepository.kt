package com.example.helloandroidcristofermunoz.data.repository

import com.example.helloandroidcristofermunoz.data.dao.TransactionDao
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.remote.firebase.FirebaseRepository
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    private val firebaseRepository = FirebaseRepository()

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun insert(transaction: Transaction) {

        // Guarda localmente
        transactionDao.insert(transaction)

        // Guarda en Firebase
        try {
            firebaseRepository.saveTransaction(transaction)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    suspend fun syncTransactions() {

        try {

            val firebaseTransactions = firebaseRepository.getTransactions()

            firebaseTransactions.forEach { transaction -> transactionDao.insert(transaction) }
        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    suspend fun update(transaction: Transaction) = transactionDao.update(transaction)

    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)

    suspend fun getTransactionById(id: Int): Transaction? {
        return transactionDao.getTransactionById(id)
    }
}
