package com.example.portforlio.presentation

import android.content.Context
import android.util.Log
import com.example.portforlio.Constants
import org.eclipse.paho.mqttv5.client.IMqttMessageListener
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttCallback
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence

class MQTTConnectionService(
    serverURI: String,
    clientID: String
) {
    private val mqttClient = MqttClient(serverURI, clientID, MemoryPersistence())

fun connect(
    username: String,
    password: String,
    cbClient: MqttCallback,
) {
    mqttClient.setCallback(cbClient)
    val options = MqttConnectionOptions()
    options.userName = username
    options.password = password.toByteArray()

    try {
        mqttClient.connectWithResult(options)
    } catch (e: MqttException) {
        e.printStackTrace()
    }
}

    fun subscribe(
        topic: String,
        qos: Int = 1,
        cbSubscribe: IMqttMessageListener,
    ) {
        try {
            mqttClient.subscribe(topic, qos,cbSubscribe)
        } catch (e: MqttException) {
            Log.d("MQTT", e.message.toString())
        }
    }
//
//    fun unsubscribe(
//        topic: String,
//        cbUnsubscribe: IMqttActionListener,
//    ) {
//        try {
//            mqttClient.unsubscribe(topic, null, cbUnsubscribe)
//        } catch (e: MqttException) {
//            e.printStackTrace()
//        }
//    }
//
    fun triggerStop(){
        publish(Constants.topic, "LEFT:0")
    }
    fun publish(
        topic: String,
        msg: String,
        qos: Int = 1,
        retained: Boolean = false
    ) {
        try {
            mqttClient.publish(topic, msg.toByteArray(), qos, retained)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
//
//    fun disconnect(cbDisconnect: IMqttActionListener) {
//        try {
//            mqttClient.disconnect(null, cbDisconnect)
//        } catch (e: MqttException) {
//            e.printStackTrace()
//        }
//    }
}
