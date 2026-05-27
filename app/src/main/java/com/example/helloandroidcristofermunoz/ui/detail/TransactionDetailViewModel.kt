package com.example.helloandroidcristofermunoz.ui.detail

import androidx.lifecycle.*
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transaction = MutableLiveData<Transaction?>()
    val transaction: LiveData<Transaction?> = _transaction

    fun loadTransaction(id: Int) {
        viewModelScope.launch {
            val result = repository.getTransactionById(id)
            _transaction.postValue(result)
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            val transaction = repository.getTransactionById(id)
            repository.deleteTransaction(id)
            
            // Si es una transacción de ahorro, decrementar las cuotas pagadas
            transaction?.let {
                if (it.category == "Ahorro") {
                    repository.decrementSavingsInstallments(it.userId, it.monthYear, it.amount)
                }
            }
        }
    }
}