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

    fun saveSettings(monthlyIncome: String, fixedExpenses: String, monthlyDebts: String, monthlyGoal: String, months: String, isMultiMonth: Boolean) {
        when {
            monthlyIncome.isBlank() -> {
                _errorMessage.value = "Los ingresos mensuales son requeridos"
                return
            }
            monthlyGoal.isBlank() -> {
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
                val currentUser = userDao.getLoggedInUser()

                if (currentUser != null) {
                    val calendar = java.util.Calendar.getInstance()
                    val monthYear = calendar.get(java.util.Calendar.YEAR) * 100 + (calendar.get(java.util.Calendar.MONTH) + 1)

                    val parsedIncome = AmountFormatter.parse(monthlyIncome)
                    val parsedExpenses = if (fixedExpenses.isNotEmpty()) AmountFormatter.parse(fixedExpenses) else 0.0
                    val parsedDebts = if (monthlyDebts.isNotEmpty()) AmountFormatter.parse(monthlyDebts) else 0.0
                    val parsedMonthlyGoal = AmountFormatter.parse(monthlyGoal)

                    // Calcular el disponible para ahorro
                    val availableForSavings = parsedIncome - parsedExpenses - parsedDebts

                    // Si es multimonth, calcular el ahorro mensual
                    val finalMonthlyGoal = if (isMultiMonth) {
                        val monthsCount = months.toIntOrNull() ?: 1
                        parsedMonthlyGoal / monthsCount
                    } else {
                        parsedMonthlyGoal
                    }

                    // Validar que la meta no supere el disponible
                    if (finalMonthlyGoal > availableForSavings) {
                        _errorMessage.value = "La meta de ahorro supera tu disponible ($${AmountFormatter.format(availableForSavings)})"
                        _isLoading.value = false
                        return@launch
                    }

                    val existingPlan = savingsPlanDao.getActiveSavingsPlanForMonth(currentUser.id, monthYear)

                    if (existingPlan != null) {
                        val updatedPlan = existingPlan.copy(
                            monthlyGoal = finalMonthlyGoal,
                            maxAmount = availableForSavings
                        )
                        savingsPlanDao.update(updatedPlan)
                    } else {
                        val newPlan = SavingsPlan(
                            userId = currentUser.id,
                            monthlyGoal = finalMonthlyGoal,
                            maxAmount = availableForSavings,
                            currentSaved = 0.0,
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
                val currentUser = userDao.getLoggedInUser()

                if (currentUser != null) {
                    val calendar = java.util.Calendar.getInstance()
                    val monthYear = calendar.get(java.util.Calendar.YEAR) * 100 + (calendar.get(java.util.Calendar.MONTH) + 1)

                    val plan = savingsPlanDao.getActiveSavingsPlanForMonth(currentUser.id, monthYear)
                    _currentSettings.value = plan
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

    fun setContext(context: android.content.Context) {
        this.context = context
    }

    private lateinit var context: android.content.Context
}
