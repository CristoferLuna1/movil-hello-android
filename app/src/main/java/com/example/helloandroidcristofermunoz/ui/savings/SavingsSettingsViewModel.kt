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

    fun saveSettings(monthlyGoal: String, maxAmount: String) {
        when {
            monthlyGoal.isBlank() -> {
                _errorMessage.value = "La meta mensual es requerida"
                return
            }
            maxAmount.isBlank() -> {
                _errorMessage.value = "El monto máximo es requerido"
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

                    val parsedMonthlyGoal = AmountFormatter.parse(monthlyGoal)
                    val parsedMaxAmount = AmountFormatter.parse(maxAmount)

                    val existingPlan = savingsPlanDao.getActiveSavingsPlanForMonth(currentUser.id, monthYear)

                    if (existingPlan != null) {
                        val updatedPlan = existingPlan.copy(
                            monthlyGoal = parsedMonthlyGoal,
                            maxAmount = parsedMaxAmount
                        )
                        savingsPlanDao.update(updatedPlan)
                    } else {
                        val newPlan = SavingsPlan(
                            userId = currentUser.id,
                            monthlyGoal = parsedMonthlyGoal,
                            maxAmount = parsedMaxAmount,
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

    private lateinit var context: android.content.Context
}
