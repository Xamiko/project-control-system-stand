package com.example.cura.compose.alertscreen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cura.compose.alertscreen.viewModel.AlertViewModel
import com.example.cura.data.ParameterRange
import com.example.cura.data.ParametersItem

import com.example.cura.navigation.BottomNavigationBar
import com.example.cura.serverMqtt.MqttManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(
    viewModel: AlertViewModel,
    mqttManager: MqttManager,
    navigateToHome: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToAlerts: () -> Unit
) {
    val temperature by viewModel.temperature.collectAsState()
    val humidity by viewModel.humidity.collectAsState()
    val co2Level by viewModel.co2Level.collectAsState()
    val lightStatus by viewModel.lightStatus.collectAsState()
    val errorMessages by viewModel.errorMessages.collectAsState()

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val errorTimeout = 10_000L
    var isBrokerConnected by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        while (true) {
            viewModel.cleanOldErrors(timeout = errorTimeout)
            delay(5000)
        }
    }

    LaunchedEffect(mqttManager) {
        mqttManager.subscribeToTopics(
            onLedControlReceived = {  },

            onHumidityReceived = { viewModel.setHumidity(it) },
            onTemperatureReceived = { viewModel.setTemperature(it) },
            onCO2Received = { viewModel.setCo2Level(it) },
            onLightReceived = { viewModel.setlightStatus(it) },

            onErrorReceived = { key, message ->
                viewModel.addError(key, message)
            }
        )
    }

    LaunchedEffect(mqttManager) {
        while (true) {
            isBrokerConnected = mqttManager.isConnected
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Environmental monitoring", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = {  }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onHomeClick = navigateToHome,
                onSettingsClick = navigateToSettings,
                onAlertsClick = navigateToAlerts
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            ConnectionStatusCard(
                isConnected = isBrokerConnected
            )
            EnvironmentParametersCard(
                temperature = temperature,
                humidity = humidity,
                co2Level = co2Level,
                lightStatus = lightStatus
            )
            RealErrorsSection(
                errorMessages = errorMessages
            )

        }
    }
}
fun MqttManager.subscribeToTopics(
    onLedControlReceived: (String) -> Unit,
    onHumidityReceived: (String) -> Unit,
    onTemperatureReceived: (String) -> Unit,
    onCO2Received: (String) -> Unit,
    onLightReceived: (String) -> Unit,
    onErrorReceived: (String, String) -> Unit
) {
    subscribeToTopic("led_control") { message -> onLedControlReceived(message) }
    subscribeToTopic("sensor/temperature") { message -> onTemperatureReceived(message) }
    subscribeToTopic("sensor/humidity") { message -> onHumidityReceived(message) }
    subscribeToTopic("sensor/co2") { message -> onCO2Received(message) }
    subscribeToTopic("sensor/light") { message -> onLightReceived(message) }

    subscribeToTopic("error/temperature") { message -> onErrorReceived("temperature", "Temperature error: $message") }
    subscribeToTopic("error/humidity") { message -> onErrorReceived("humidity", "Humidity error: $message") }
    subscribeToTopic("error/co2") { message -> onErrorReceived("co2", "CO₂ error: $message") }
    subscribeToTopic("error/light") { message -> onErrorReceived("light", "Light error: $message") }
}

@Composable
fun EnvironmentParametersCard(
    temperature: String,
    humidity: String,
    co2Level: String,
    lightStatus: String
) {
    val paramRange = ParameterRange()

    val parameters = remember(temperature, humidity, co2Level, lightStatus) {
        listOf(
            ParametersItem("Temperature", temperature, Icons.Default.CheckCircle),
            ParametersItem("Humidity", humidity, Icons.Default.CheckCircle),
            ParametersItem("Level CO₂", co2Level, Icons.Default.CheckCircle),
            ParametersItem("Light level", lightStatus, Icons.Default.CheckCircle)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Environmental parameters",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            parameters.forEach { param ->
                val isError = when (param.name) {
                    "Temperature" ->
                        param.value.toFloatOrNull()?.let { it !in paramRange.tempRange } == true

                    "Humidity" ->
                        param.value.toFloatOrNull()?.let { it !in paramRange.humRange } == true

                    "Level CO₂" ->
                        param.value.toFloatOrNull()?.let { it !in paramRange.co2Range } == true

                    "Light level" -> {
                        param.value.toFloatOrNull()?.let { it !in paramRange.lightRange } == true


                    }

                    else -> false
                }

                if (isError) {
                    Log.w("EnvParams", "⚠️ ${param.name} is out of range: ${param.value}")
                }

                EnvironmentParameterItem(
                    name = param.name,
                    value = param.value,
                    icon = if (isError) Icons.Default.Warning else param.icon,
                    isError = isError
                )
            }
        }
    }
}
@Composable
private fun RealErrorsSection(errorMessages: Map<String, Pair<String, Long>>) {
    if (errorMessages.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Error:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                errorMessages.values.forEach { (message, _) ->
                    ErrorItem(
                        message = message,
                        details = "Current status"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


@Composable
private fun EnvironmentParameterItem(
    name: String,
    value: String,
    icon: ImageVector,
    isError: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = name,
            tint = if (isError) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isError) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
private fun ErrorItem(message: String, details: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onErrorContainer
                ),
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            details,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier.padding(start = 28.dp)
        )
    }
}


@Composable
private fun ConnectionStatusCard(isConnected: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isConnected) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = if (isConnected) "Connected" else "Error",
                tint = if (isConnected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                if (isConnected) "Connected to broker" else "Not connected",
                style = MaterialTheme.typography.titleMedium,
                color = if (isConnected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}