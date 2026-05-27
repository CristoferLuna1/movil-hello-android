package com.example.helloandroidcristofermunoz.ui.savings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.helloandroidcristofermunoz.data.AppDatabase
import com.example.helloandroidcristofermunoz.data.model.SavingsPlan
import com.example.helloandroidcristofermunoz.utils.AmountFormatter
import kotlinx.coroutines.launch

class SavingsSettingsViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _currentSettings = MutableLiveData<SavingsPlan?>()
    val currentSettings: LiveData<SavingsPlan?> = _currentSettings

    private val _calculatedIncome = MutableLiveData<Double>()
    val calculatedIncome: LiveData<Double> = _calculatedIncome

    private val _calculatedFixedExpenses = MutableLiveData<Double>()
    val calculatedFixedExpenses: LiveData<Double> = _calculatedFixedExpenses

    private val _calculatedMonthlyDebts = MutableLiveData<Double>()
    val calculatedMonthlyDebts: LiveData<Double> = _calculatedMonthlyDebts

    private val _calculatedOneTimeDebts = MutableLiveData<Double>()
    val calculatedOneTimeDebts: LiveData<Double> = _calculatedOneTimeDebts

    private val _availableForSavings = MutableLiveData<Double>()
    val availableForSavings: LiveData<Double> = _availableForSavings

    private val _recommendedMonthlySavings = MutableLiveData<Double>()
    val recommendedMonthlySavings: LiveData<Double> = _recommendedMonthlySavings

    fun saveSettings(totalGoal: String, months: String, isMultiMonth: Boolean) {
        when {
            totalGoal.isBlank() -> {
                _errorMessage.value = "La meta de ahorro es requerida"
                return
            }
            isMultiMonth && months.isBlank() -> {
                _errorMessage.value = "El número de meses es requerido"
                return
            }
            isMultiMonth && months.toIntOrNull() == null -> {
                _errorMessage.value = "El número de meses debe ser válido"
                return
            }
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val savingsPlanDao = AppDatabase.getDatabase(context).savingsPlanDao()
                val userDao = AppDatabase.getDatabase(context).userDao()
                val transactionDao = AppDatabase.getDatabase(context).transactionDao()
                val currentUser = userDao.getLoggedInUser()

                if (currentUser != null) {
                    val calendar = java.util.Calendar.getInstance()
                    val monthYear = calendar.get(java.util.Calendar.YEAR) * 100 + (calendar.get(java.util.Calendar.MONTH) + 1)

                    // Calcular ingresos y gastos desde transacciones
                    val transactions = transactionDao.getAllTransactionsByUser(currentUser.id)
                    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
                    
                    // Gastos fijos mensuales (isMonthlyPersistent)
                    val fixedExpenses = transactions.filter { 
                        it.type == "expense" && it.isMonthlyPersistent && it.category == "Gastos Fijos" 
                    }.sumOf { it.amount }
                    
                    // Deudas mensuales (isMonthlyPersistent)
                    val monthlyDebts = transactions.filter { 
                        it.type == "expense" && it.isMonthlyPersistent && it.category == "Deudas Mensuales" 
                    }.sumOf { it.amount }
                    
                    // Deudas únicas del mes actual (no persistentes)
                    val oneTimeDebts = transactions.filter { 
                        it.type == "expense" && !it.isMonthlyPersistent && it.monthYear == monthYear 
                    }.sumOf { it.amount }
                    
                    val parsedTotalGoal = AmountFormatter.parse(totalGoal)
                    
                    // Calcular el disponible para ahorro (sin contar deudas únicas)
                    val availableForSavings = income - fixedExpenses - monthlyDebts
                    
                    // Si es multimonth, calcular el ahorro mensual base
                    val baseMonthlyGoal = if (isMultiMonth) {
                        val monthsCount = months.toIntOrNull() ?: 1
                        parsedTotalGoal / monthsCount
                    } else {
                        parsedTotalGoal
                    }

                    // Validar que la meta mensual no supere el disponible
                    if (baseMonthlyGoal > availableForSavings) {
                        _errorMessage.value = "La meta de ahorro mensual ($${AmountFormatter.format(baseMonthlyGoal)}) supera tu disponible ($${AmountFormatter.format(availableForSavings)})"
                        _isLoading.value = false
                        return@launch
                    }

                    // Actualizar los LiveData con los valores calculados
                    _calculatedIncome.value = income
                    _calculatedFixedExpenses.value = fixedExpenses
                    _calculatedMonthlyDebts.value = monthlyDebts
                    _calculatedOneTimeDebts.value = oneTimeDebts
                    _availableForSavings.value = availableForSavings
                    _recommendedMonthlySavings.value = baseMonthlyGoal

                    val existingPlan = savingsPlanDao.getActiveSavingsPlanForMonth(currentUser.id, monthYear)

                    if (existingPlan != null) {
                        val updatedPlan = existingPlan.copy(
                            totalGoal = parsedTotalGoal,
                            monthlyGoal = baseMonthlyGoal,
                            maxAmount = availableForSavings,
                            totalMonths = if (isMultiMonth) months.toIntOrNull() ?: 1 else 1
                        )
                        savingsPlanDao.update(updatedPlan)
                    } else {
                        val newPlan = SavingsPlan(
                            userId = currentUser.id,
                            totalGoal = parsedTotalGoal,
                            monthlyGoal = baseMonthlyGoal,
                            maxAmount = availableForSavings,
                            currentSaved = 0.0,
                            accumulatedDeficit = 0.0,
                            totalMonths = if (isMultiMonth) months.toIntOrNull() ?: 1 else 1,
                            paidInstallments = 0,
                            monthYear = monthYear,
                            isActive = true
                        )
                        savingsPlanDao.insert(newPlan)
                    }

                    _isSuccess.value = true
                } else {
                    _errorMessage.value = "No hay usuario logueado"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar configuración"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCurrentSettings() {
        viewModelScope.launch {
            try {
                val savingsPlanDao = AppDatabase.getDatabase(context).savingsPlanDao()
                val userDao = AppDatabase.getDatabase(context).userDao()
                val transactionDao = AppDatabase.getDatabase(context).transactionDao()
                val currentUser = userDao.getLoggedInUser()

                if (currentUser != null) {
                    val calendar = java.util.Calendar.getInstance()
                    val monthYear = calendar.get(java.util.Calendar.YEAR) * 100 + (calendar.get(java.util.Calendar.MONTH) + 1)

                    val plan = savingsPlanDao.getActiveSavingsPlanForMonth(currentUser.id, monthYear)
                    _currentSettings.value = plan

                    // Calcular ingresos y gastos desde transacciones
                    val transactions = transactionDao.getAllTransactionsByUser(currentUser.id)
                    val income = transactions.filter { it.type == "income" }.sumOf { it.amount }
                    
                    // Gastos fijos mensuales
                    val fixedExpenses = transactions.filter { 
                        it.type == "expense" && it.isMonthlyPersistent && it.category == "Gastos Fijos" 
                    }.sumOf { it.amount }
                    
                    // Deudas mensuales
                    val monthlyDebts = transactions.filter { 
                        it.type == "expense" && it.isMonthlyPersistent && it.category == "Deudas Mensuales" 
                    }.sumOf { it.amount }
                    
                    // Deudas únicas del mes actual
                    val oneTimeDebts = transactions.filter { 
                        it.type == "expense" && !it.isMonthlyPersistent && it.monthYear == monthYear 
                    }.sumOf { it.amount }
                    
                    val available = income - fixedExpenses - monthlyDebts

                    _calculatedIncome.value = income
                    _calculatedFixedExpenses.value = fixedExpenses
                    _calculatedMonthlyDebts.value = monthlyDebts
                    _calculatedOneTimeDebts.value = oneTimeDebts
                    _availableForSavings.value = available
                    
                    // Calcular ahorro recomendado del mes actual (considerando deudas únicas)
                    val monthlyAvailable = available - oneTimeDebts
                    _recommendedMonthlySavings.value = monthlyAvailable
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun resetSuccessState() {
        _isSuccess.value = false
    }

    fun resetErrorState() {
        _errorMessage.value = null
    }

    fun cancelPlan() {
        viewModelScope.launch {
            try {
                val savingsPlanDao = AppDatabase.getDatabase(context).savingsPlanDao()
                val userDao = AppDatabase.getDatabase(context).userDao()
                val currentUser = userDao.getLoggedInUser()

                if (currentUser != null) {
                    val calendar = java.util.Calendar.getInstance()
                    val monthYear = calendar.get(java.util.Calendar.YEAR) * 100 + (calendar.get(java.util.Calendar.MONTH) + 1)

                    val existingPlan = savingsPlanDao.getActiveSavingsPlanForMonth(currentUser.id, monthYear)
                    existingPlan?.let {
                        val deactivatedPlan = it.copy(isActive = false)
                        savingsPlanDao.update(deactivatedPlan)
                        _isSuccess.value = true
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cancelar plan"
            }
        }
    }

    fun setContext(context: android.content.Context) {
        this.context = context
    }

    private lateinit var context: android.content.Context
}
