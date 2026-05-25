package com.example.helloandroidcristofermunoz.ui.addtransaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _titleError = MutableLiveData<String?>()
    val titleError: LiveData<String?> = _titleError

    private val _amountError = MutableLiveData<String?>()
    val amountError: LiveData<String?> = _amountError

    private val _categoryError = MutableLiveData<String?>()
    val categoryError: LiveData<String?> = _categoryError

    private val _editingTransaction = MutableLiveData<Transaction?>()
    val editingTransaction: LiveData<Transaction?> = _editingTransaction

    fun setEditingTransaction(transaction: Transaction) {
        _editingTransaction.value = transaction
    }

    fun validateAndSaveTransaction(
        title: String,
        amount: String,
        category: String,
        type: String
    ) {

        _titleError.value = null
        _amountError.value = null
        _categoryError.value = null

        var isValid = true

        if (title.isBlank()) {
            _titleError.value = "El título es requerido"
            isValid = false
        }

        if (amount.isBlank()) {
            _amountError.value = "El monto es requerido"
            isValid = false
        }

        if (category.isBlank()) {
            _categoryError.value = "La categoría es requerida"
            isValid = false
        }

        if (!isValid) return

        val currentEditing = _editingTransaction.value

        if (currentEditing != null) {
            updateTransaction(currentEditing.id, title, amount, category, type)
        } else {
            saveTransaction(title, amount, category, type)
        }
    }

    private fun saveTransaction(
        title: String,
        amount: String,
        category: String,
        type: String
    ) {

        _isLoading.value = true

        viewModelScope.launch {

            try {

                val transaction = Transaction(
                    title = title,
                    amount = amount.toDouble(),
                    category = category,
                    type = type,
                    date = System.currentTimeMillis()
                )

                repository.insert(transaction)

                _isLoading.value = false
                _isSuccess.value = true

            } catch (e: Exception) {

                _isLoading.value = false
                _errorMessage.value = "Error al guardar la transacción"
            }
        }
    }

    private fun updateTransaction(
        id: Int,
        title: String,
        amount: String,
        category: String,
        type: String
    ) {

        _isLoading.value = true

        viewModelScope.launch {

            try {

                val transaction = Transaction(
                    id = id,
                    title = title,
                    amount = amount.toDouble(),
                    category = category,
                    type = type,
                    date = System.currentTimeMillis()
                )

                repository.update(transaction)

                _isLoading.value = false
                _isSuccess.value = true
                _editingTransaction.value = null

            } catch (e: Exception) {

                _isLoading.value = false
                _errorMessage.value = "Error al actualizar la transacción"
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                repository.delete(transaction)
                _isLoading.value = false
                _isSuccess.value = true
                _editingTransaction.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error al eliminar la transacción"
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                repository.update(transaction)
                _isLoading.value = false
                _isSuccess.value = true
                _editingTransaction.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error al actualizar la transacción"
            }
        }
    }

    fun resetSuccessState() {
        _isSuccess.value = false
    }

    fun resetErrorState() {
        _errorMessage.value = null
    }

    fun clearEditing() {
        _editingTransaction.value = null
    }
}
