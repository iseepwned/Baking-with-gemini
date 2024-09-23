package com.example.aptstarter

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.CompletableFuture

class WebSocket(
    private val onMessageReceived: (String, String) -> Unit,
    private val onChatRequest: (String, String) -> Unit,
    private val onUserStatusChanged: (String, Boolean) -> Unit
) {

    private var webSocketClient: WebSocketClient? = null
    private var webSocketClientPrivate: WebSocketClient? = null
    private var isConnectedGlobal: Boolean = false
    private var isConnectedPrivate: Boolean = false
    private var randomName: String? = null

    fun generateRandomString(length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    fun generateRandomName(): String {
        val firstName = generateRandomString(5)
        return "$firstName"
    }

    fun connect() {
        webSocketClient = object : WebSocketClient(URI("ws://10.0.2.2:8080/chat")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                isConnectedGlobal = true
                randomName = "APP"?: generateRandomName()
                send(randomName)
            }
            override fun onMessage(message: String?) {
                message?.let {
                    when {
                        it.startsWith("REQUEST_FROM:") -> {
                            val userId = it.removePrefix("REQUEST_FROM:")
                            onChatRequest(randomName?: "bad name", userId)
                        }
                        else -> {
                            onMessageReceived(randomName?: "bad name", message ?: "" )
                        }
                    }
                }
            }
            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                isConnectedGlobal = false
            }
            override fun onError(ex: Exception?) {
                ex?.printStackTrace()
            }
        }
        webSocketClient?.connect()
    }


    fun connectPrivate(randomName: String) : CompletableFuture<Unit> {
        webSocketClient?.close()
        val future = CompletableFuture<Unit>()
        webSocketClientPrivate = object : WebSocketClient(URI("ws://10.0.2.2:8080/chat/$randomName")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                isConnectedPrivate = true
                future.complete(Unit)
            }
            override fun onMessage(message: String?) {
                message?.let {
                    when {
                        it.startsWith("REQUEST_FROM:") -> {
                            val userId = it.removePrefix("REQUEST_FROM:")
                            onChatRequest(randomName, userId)
                        }
                        it.startsWith("USER_CONNECTED:") -> {
                            val userId = it.removePrefix("USER_CONNECTED:")
                            onUserStatusChanged(userId, true)
                        }
                        it.startsWith("USER_DISCONNECTED:") -> {
                            val disconnectedId = it.removePrefix("USER_DISCONNECTED:")
                            onUserStatusChanged(disconnectedId, false)
                        }
                        else -> {
                            onMessageReceived(randomName, message)
                        }
                    }
                }
            }
            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                isConnectedPrivate = false
            }
            override fun onError(ex: Exception?) {
                ex?.printStackTrace()
            }
        }

        webSocketClientPrivate?.connect()
        return future
    }

    fun sendMessage(userId : String, message: String) {
        if (isConnectedPrivate) {
            webSocketClientPrivate?.send("$userId:$message")
        } else {
            // Handle the case when the WebSocket is not connected
            println("WebSocket is not connected")
        }
    }

    fun sendMessageGlobal(message: String) {
        if (isConnectedGlobal) {
            webSocketClient?.send(message)
        } else {
            // Handle the case when the WebSocket is not connected
            println("WebSocket is not connected")
        }
    }

    fun closeGlobal() {
        webSocketClient?.close()
    }

    fun closePrivate() {
        webSocketClientPrivate?.close()
    }
}
