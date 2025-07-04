package com.example.cura.data

import androidx.compose.ui.graphics.vector.ImageVector


data class ParametersItem(
    val name: String,
    val value: String,
    val icon: ImageVector,
    val isError: Boolean = false
)

data class ParameterRange(
    val tempRange: ClosedRange<Float> = 0.0f..30.0f,
    val humRange: ClosedRange<Float> = 0.0f..100.0f,
    val co2Range: ClosedRange<Float> = 0.0f..5000.0f,
    val lightRange: ClosedRange<Float> = 0.0f..10000.0f
)