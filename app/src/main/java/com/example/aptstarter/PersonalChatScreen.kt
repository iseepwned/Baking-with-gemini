package com.example.aptstarter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.concurrent.CompletableFuture

@Composable
fun PersonalChatScreen(nameRandom: String, partnerName: String, requestAccepted: String, navController: NavController) {
    var messages by remember { mutableStateOf(listOf<String>()) }
    var inputMessage by remember { mutableStateOf("") }
    var randomName by remember { mutableStateOf(nameRandom) }


    val webSocket = remember {
        WebSocket(
            onMessageReceived = { randomName, message ->
                messages = messages + message
            },
            onChatRequest = {_, _ -> },
            onUserStatusChanged = { name, isNew ->
                messages = if (isNew) {
                    messages + "New user connected:$name"
                } else {
                    messages + "User disconnected:$name"
                }
            }
        )
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Chat with $partnerName", style = MaterialTheme.typography.headlineSmall)

        LaunchedEffect(Unit) {
            val future: CompletableFuture<Unit> = webSocket.connectPrivate(randomName)

            future.whenComplete { _, _ ->
                if (requestAccepted == "true") {
                    webSocket.sendMessage("ACCEPTED_REQUEST", partnerName)
                } else {
                    webSocket.sendMessage("REQUEST_CHAT", partnerName)
                }

            }

        }
        DisposableEffect(Unit) {
            onDispose {
                webSocket.closePrivate()
            }
        }

        LazyColumn(
            reverseLayout = false,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            items(messages) { message ->
                MessageItem(nameRandom, message,null)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = inputMessage,
                onValueChange = { inputMessage = it },
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White)
                    .padding(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (inputMessage.isNotBlank()) {
                        webSocket.sendMessage(partnerName, inputMessage)
                        inputMessage = ""
                    }
                }
            ) {
                Text("Send")
            }
        }

        Button(
            onClick = { navController.navigateUp() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Back to General Chat")
        }

    }
}

@Composable
fun ChatRequestDialog(senderId: String, onResponse: (Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = { onResponse(false) },
        title = { Text("Chat Request") },
        text = { Text("User $senderId wants to chat with you. Accept?") },
        confirmButton = {
            Button(onClick = { onResponse(true) }) {
                Text("Accept")
            }
        },
        dismissButton = {
            Button(onClick = { onResponse(false) }) {
                Text("Reject")
            }
        }
    )
}