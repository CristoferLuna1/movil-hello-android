package com.example.helloandroidcristofermunoz.ui.addtransaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddTransactionViewModel : ViewModel() {

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

    fun validateAndSaveTransaction(
        title: String,
        amount: String,
        category: String,
        type: String
    ) {
        // Reset errors
        _titleError.value = null
        _amountError.value = null
        _categoryError.value = null

        var isValid = true

        // Validar título
        if (title.isBlank()) {
            _titleError.value = "El título es requerido"
            isValid = false
        } else if (title.length < 3) {
            _titleError.value = "El título debe tener al menos 3 caracteres"
            isValid = false
        }

        // Validar monto
        if (amount.isBlank()) {
            _amountError.value = "El monto es requerido"
            isValid = false
        } else {
            try {
                val amountValue = amount.toDouble()
                if (amountValue <= 0) {
                    _amountError.value = "El monto debe ser mayor a 0"
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                _amountError.value = "Monto inválido"
                isValid = false
            }
        }

        // Validar categoría
        if (category.isBlank()) {
            _categoryError.value = "La categoría es requerida"
            isValid = false
        } else if (category.length < 3) {
            _categoryError.value = "La categoría debe tener al menos 3 caracteres"
            isValid = false
        }

        if (isValid) {
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

        // Simular guardado (en el futuro esto se conectará con el repositorio)
        try {
            Thread.sleep(1000) // Simular delay de red
            _isLoading.value = false
            _isSuccess.value = true
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "Error al guardar la transacción"
        }
    }

    fun resetSuccessState() {
        _isSuccess.value = false
    }

    fun resetErrorState() {
        _errorMessage.value = null
    }
}
