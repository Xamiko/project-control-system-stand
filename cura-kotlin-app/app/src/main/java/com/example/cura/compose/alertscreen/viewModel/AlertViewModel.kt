package com.example.cura.compose.alertscreen.viewModel


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AlertViewModel: ViewModel() {
    private val _temperature = MutableStateFlow("Waiting for data")
    val temperature: StateFlow<String> = _temperature

    private val _humidity = MutableStateFlow("Waiting for data")
    val humidity: StateFlow<String> = _humidity

    private val _co2Level = MutableStateFlow("Waiting for data")
    val co2Level: StateFlow<String> = _co2Level

    private val _lightStatus = MutableStateFlow("Waiting for data")
    val lightStatus: StateFlow<String> = _lightStatus


    private val _errorMessages = MutableStateFlow<Map<String, Pair<String, Long>>>(emptyMap())
    val errorMessages: StateFlow<Map<String, Pair<String, Long>>> = _errorMessages


    fun setTemperature(value: String) {
        _temperature.value = value
    }

    fun setHumidity(value: String) {
        _humidity.value = value
    }

    fun setCo2Level(value: String) {
        _co2Level.value = value
    }

    fun setlightStatus(value: String) {
        _lightStatus.value = value
    }
    fun addError(key: String, message: String) {
        _errorMessages.value = _errorMessages.value.toMutableMap().apply {
            this[key] = message to System.currentTimeMillis()
        }
    }
    fun cleanOldErrors(timeout: Long) {
        val currentTime = System.currentTimeMillis()
        _errorMessages.value = _errorMessages.value.filterValues { (_, timestamp) ->
            currentTime - timestamp < timeout
        }
    }



}