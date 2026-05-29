package com.example.helloandroidcristofermunoz.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object NetworkState {

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected

    fun update(value: Boolean) {
        _isConnected.postValue(value)
    }
}