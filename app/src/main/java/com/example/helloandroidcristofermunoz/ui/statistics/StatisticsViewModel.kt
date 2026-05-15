package com.example.helloandroidcristofermunoz.ui.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StatisticsViewModel : ViewModel() {

    private val _income =
        MutableLiveData<Double>()

    val income: LiveData<Double>
        get() = _income

    private val _expenses =
        MutableLiveData<Double>()

    val expenses: LiveData<Double>
        get() = _expenses

    init {

        _income.value = 2500000.0

        _expenses.value = 158000.0
    }
}