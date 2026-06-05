package com.example.helloandroidcristofermunoz.ui.detail

import androidx.lifecycle.*
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class TransactionDetailViewModel(private val repository: TransactionRepository) : ViewModel() {
    private val _transaction = MutableLiveData<Transaction?>()
    val transaction: LiveData<Transaction?> = _transaction

    private val _deleteDone = MutableLiveData<Boolean>()
    val deleteDone: LiveData<Boolean> = _deleteDone

    fun loadTransaction(id: Int) {

        viewModelScope.launch {
            val result = repository.getTransactionById(id)

            _transaction.postValue(result)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)

            if (transaction.category == "Ahorro") {
                repository.decrementSavingsInstallments(
                        transaction.userId,
                        transaction.monthYear,
                        transaction.amount
                )
            }

            _deleteDone.postValue(true)
        }
    }
}
