package com.example.cura.serverMqtt

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttManager private constructor(private val context: Context) {
    private var mqttClient: MqttClient? = null
    private val brokerUrl = "tcp://192.168.0.112:1883"
    var isConnected = false

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: MqttManager? = null

        fun getInstance(context: Context): MqttManager {
            return instance ?: synchronized(this) {
                instance ?: MqttManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun connect() {
        if (isConnected) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                mqttClient = MqttClient(brokerUrl, MqttClient.generateClientId(), null)
                val options = MqttConnectOptions().apply {
                    isCleanSession = true
                    connectionTimeout = 10
                    keepAliveInterval = 60
                    isAutomaticReconnect = true
                }

                mqttClient?.connect(options)
                isConnected = true

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Сonnect broker", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error connect: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    data class MqttCallbacks(
        val onConnected: () -> Unit = {},
        val onConnectionFailed: (Throwable) -> Unit = {},
        val onLedControlReceived: (String) -> Unit,
        val onHumidityReceived: (String) -> Unit,
        val onTemperatureReceived: (String) -> Unit,
        val onCO2Received: (String) -> Unit,
        val onLightReceived: (String) -> Unit,
        val onErrorReceived: (String, String) -> Unit
    )

    fun subscribeToTopics(callbacks: MqttCallbacks) {
        subscribeToTopic("led_control") { message -> callbacks.onLedControlReceived(message) }
        subscribeToTopic("sensor/temperature") { message -> callbacks.onTemperatureReceived(message) }
        subscribeToTopic("sensor/humidity") { message -> callbacks.onHumidityReceived(message) }
        subscribeToTopic("sensor/co2") { message -> callbacks.onCO2Received(message) }
        subscribeToTopic("sensor/light") { message -> callbacks.onLightReceived(message) }

        subscribeToTopic("error/temperature") { message -> callbacks.onErrorReceived("temperature", "Temperature error: $message") }
        subscribeToTopic("error/humidity") { message -> callbacks.onErrorReceived("humidity", "Humidity error: $message") }
        subscribeToTopic("error/co2") { message -> callbacks.onErrorReceived("co2", "CO₂ error: $message") }
        subscribeToTopic("error/light") { message -> callbacks.onErrorReceived("light", "Light error: $message") }
    }




    fun subscribeToTopic(topic: String, onMessageReceived: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = mqttClient
            if (client == null || !client.isConnected) {
                withContext(Dispatchers.Main) {
                }
                return@launch
            }

            try {
                client.subscribe(topic) { _, message ->
                    val receivedMessage = String(message.payload)
                    onMessageReceived(receivedMessage)

                }

                withContext(Dispatchers.Main) {

                }
            } catch (e: MqttException) {

                withContext(Dispatchers.Main) {
                }
                e.printStackTrace()
            }
        }
    }


    fun disconnect() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                mqttClient?.disconnect()
                mqttClient?.close()
                isConnected = false
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Disconnected from the broker", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun publishMessage(topic: String, message: String, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = mqttClient
            if (client == null || !client.isConnected) {
                withContext(Dispatchers.Main) {
                }
                return@launch
            }

            try {
                client.publish(topic, MqttMessage(message.toByteArray()))
                withContext(Dispatchers.Main) {
                    showToast(context, "Posted to topic $topic")
                }
            } catch (e: MqttException) {
                withContext(Dispatchers.Main) {
                    showToast(context, "Error sending message: ${e.message}")
                }
                e.printStackTrace()
            }
        }
    }
    private fun showToast(context: Context, message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}