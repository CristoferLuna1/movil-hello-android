package com.example.helloandroidcristofermunoz.ui.addtransaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.model.Transaction
import com.example.helloandroidcristofermunoz.data.repository.TransactionRepository
import com.example.helloandroidcristofermunoz.utils.AmountFormatter
import kotlinx.coroutines.launch
import java.util.Calendar

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

    private val _paymentDayError = MutableLiveData<String?>()
    val paymentDayError: LiveData<String?> = _paymentDayError

    fun validateAndSaveTransaction(
        title: String,
        amount: String,
        category: String,
        type: String,
        paymentDay: String,
        endDate: Long?,
        isMonthlyPersistent: Boolean
    ) {

        _titleError.value = null
        _amountError.value = null
        _categoryError.value = null
        _paymentDayError.value = null

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

        // Validar día de pago si es deuda mensual
        if (isMonthlyPersistent && paymentDay.isBlank()) {
            _paymentDayError.value = "El día de pago es requerido para deudas mensuales"
            isValid = false
        }

        if (!isValid) return

        saveTransaction(title, amount, category, type, paymentDay, endDate, isMonthlyPersistent)
    }

    private fun saveTransaction(
        title: String,
        amount: String,
        category: String,
        type: String,
        paymentDay: String,
        endDate: Long?,
        isMonthlyPersistent: Boolean
    ) {

        _isLoading.value = true

        viewModelScope.launch {

            try {

                val calendar = Calendar.getInstance()
                val monthYear = calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)

                val parsedAmount = AmountFormatter.parse(amount)
                val parsedPaymentDay = if (paymentDay.isNotBlank()) paymentDay.toIntOrNull() else null

                val transaction = Transaction(
                    title = title,
                    amount = parsedAmount,
                    category = category,
                    customCategory = if (category == "Otro") category else null,
                    type = type,
                    date = System.currentTimeMillis(),
                    paymentDay = parsedPaymentDay,
                    endDate = endDate,
                    isMonthlyPersistent = isMonthlyPersistent,
                    monthYear = monthYear,
                    userId = 1 // TODO: Obtener el ID del usuario actual
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

    fun resetSuccessState() {
        _isSuccess.value = false
    }

    fun resetErrorState() {
        _errorMessage.value = null
    }
}