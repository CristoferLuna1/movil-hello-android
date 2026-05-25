package com.example.helloandroidcristofermunoz.data.repository

import com.example.helloandroidcristofermunoz.data.dao.TransactionDao
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.remote.firebase.FirebaseRepository
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    private val firebaseRepository = FirebaseRepository()

    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun getTotalBalance(): Double = transactionDao.getTotalBalance() ?: 0.0
    suspend fun getTotalIncome(): Double = transactionDao.getTotalIncome() ?: 0.0
    suspend fun getTotalExpenses(): Double = transactionDao.getTotalExpenses() ?: 0.0

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
            transactionDao.getTransactionsByDateRange(startDate, endDate)

    fun searchTransactions(searchQuery: String): Flow<List<Transaction>> =
            transactionDao.searchTransactions(searchQuery)

    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)

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
