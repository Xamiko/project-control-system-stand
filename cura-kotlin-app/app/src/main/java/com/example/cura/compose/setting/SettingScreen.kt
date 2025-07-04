package com.example.cura.compose.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cura.navigation.BottomNavigationBar
import com.example.cura.serverMqtt.MqttManager
import androidx.compose.foundation.rememberScrollState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    mqttManager: MqttManager,
    navigateToHome: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToAlerts: () -> Unit
) {
    var tempRange by remember { mutableStateOf(20f..30f) }
    var humRange by remember { mutableStateOf(30f..60f) }
    var co2Threshold by remember { mutableStateOf(800f) }
    var lightThreshold by remember { mutableStateOf(50f) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setting", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { /*  */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Меню",
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
                .padding(16.dp)
        ) {

            ThresholdCard(
                title = "Temperature",
                value = "${tempRange.start.toInt()}°C - ${tempRange.endInclusive.toInt()}°C",
                content = {
                    RangeSlider(
                        value = tempRange,
                        onValueChange = { tempRange = it },
                        valueRange = 0f..50f,
                        steps = 49,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            ThresholdCard(
                title = "Humidity",
                value = "${humRange.start.toInt()}% - ${humRange.endInclusive.toInt()}%",
                content = {
                    RangeSlider(
                        value = humRange,
                        onValueChange = { humRange = it },
                        valueRange = 0f..100f,
                        steps = 99,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ThresholdCard(
                title = "CO₂",
                value = "${co2Threshold.toInt()} ppm",
                content = {
                    Slider(
                        value = co2Threshold,
                        onValueChange = { co2Threshold = it },
                        valueRange = 400f..2000f,
                        steps = 159,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))


            ThresholdCard(
                title = "Illumination",
                value = "${lightThreshold.toInt()} lux",
                content = {
                    Slider(
                        value = lightThreshold,
                        onValueChange = { lightThreshold = it },
                        valueRange = 0f..1000f,
                        steps = 99,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = {


                    //mqttManager.publishMessage("settings/temp/min", tempRange.start.toString())
                    //mqttManager.publishMessage("settings/temp/max", tempRange.endInclusive.toString())
                    //mqttManager.publishMessage("settings/hum/min", humRange.start.toString())
                    //mqttManager.publishMessage("settings/hum/max", humRange.endInclusive.toString())
                    //mqttManager.publishMessage("settings/co2", co2Threshold.toString())
                    //mqttManager.publishMessage("settings/light", lightThreshold.toString())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Save settings", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ThresholdCard(
    title: String,
    value: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}
