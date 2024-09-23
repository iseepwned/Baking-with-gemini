package com.example.aptstarter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.random.Random

@Composable
fun ChatScreenWebSocket(navController: NavController) {
    var messages by remember { mutableStateOf(listOf<String>()) }
    var activesUsers by remember { mutableStateOf(String()) }
    var inputMessage by remember { mutableStateOf("") }
    var randomName2 by remember { mutableStateOf("") }
    var chatRequestSender by remember { mutableStateOf<String?>(null) }
        val webSocket = remember {
            WebSocket(
                onMessageReceived = { randomName, message ->
                    val delimiter = "ACTIVE_USERS:"
                    val parts = message.split(delimiter, limit = 2)
                    messages = messages +     parts[0]
                    randomName2 = randomName
                    activesUsers = parts[1]
                },
                onChatRequest = {
                       randomName, senderId ->
                    chatRequestSender = senderId
                    randomName2 = randomName
                },
                onUserStatusChanged = {
                    _, _ ->
                }
            )
        }
    LaunchedEffect(Unit) {
        webSocket.connect()
    }
    DisposableEffect(Unit) {
        onDispose {
            webSocket.closeGlobal()
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        LazyColumn(
            reverseLayout = false,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            items(messages) { message ->
                MessageItem(randomName2, message, navController)
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
                        webSocket.sendMessageGlobal(inputMessage)
                        inputMessage = ""
                    }
                }
            ) {
                Text("Send")
            }
        }
        // Mostrar la lista de usuarios activos
        ActiveUsersDisplay(activesUsers = activesUsers)

        // Show chat request dialog if there is one
        chatRequestSender?.let { senderId ->
            ChatRequestDialog(senderId) { accepted ->
                if (accepted) {
                    navController.navigate("personal_chat/$senderId/$randomName2/true")
                }
                chatRequestSender = null
            }
        }
}

}
@Composable
fun ActiveUsersDisplay(activesUsers: String) {
    if (activesUsers.isNotEmpty()) {
        Row() {
            Text("Active Users:", style = MaterialTheme.typography.labelSmall)
            Text(
                activesUsers,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
fun getRandomColor(): Color {
    return Color(
        red = Random.nextFloat(),
        green = Random.nextFloat(),
        blue = Random.nextFloat()
    )
}
val colorMap =  mutableMapOf<String, Color>()
@Composable
fun rememberColorForName(name: String): Color {
    return remember(name) {
        colorMap[name] ?: run {
            val newColor = getRandomColor()
            colorMap[name] = newColor
            newColor
        }
    }
}
@Composable
fun MessageItem(randomName2: String, message: String, navController: NavController?) {
    val split = message.split(":")
    val firstSplit = split[0]
    val formattedText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = rememberColorForName(split[0]))) {
            // Annotate the clickable part
            pushStringAnnotation(tag = "NAME", annotation = split[0])
            append("${split[0]}: ")
            pop()
        }
        withStyle(style = SpanStyle(color = Color.White)) {
            append(split.getOrElse(1) { "" })
        }
    }

    Text(
        text = formattedText,
        modifier = Modifier
            .padding(8.dp)
            .clickable {
                println("Name clicked: $formattedText")
                navController?.navigate("personal_chat/$firstSplit/$randomName2/false")
            },
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Start
    )
}
