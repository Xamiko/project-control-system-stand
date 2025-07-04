package com.example.cura.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cura.compose.alertscreen.AlertScreen
import com.example.cura.compose.alertscreen.viewModel.AlertViewModel
import com.example.cura.compose.setting.SettingsScreen
import com.example.cura.serverMqtt.MqttManager

@Composable
fun NavigationApp(mqttManager: MqttManager) {
    val navController = rememberNavController()
    val viewModel: AlertViewModel = viewModel()
    NavHost(navController, startDestination = "alerts") {

        composable("alerts") {
            AlertScreen(
                viewModel = viewModel,
                mqttManager = mqttManager,
                navigateToHome = { navController.navigate("main") },
                navigateToSettings = { navController.navigate("settings") },
                navigateToAlerts = { navController.navigate("alerts") }
            )
        }
        composable("settings") {
            SettingsScreen(
                mqttManager = mqttManager,
                navigateToHome = { navController.navigate("main") },
                navigateToSettings = { navController.navigate("settings") },
                navigateToAlerts = { navController.navigate("alerts") }
            )
        }



    }
}