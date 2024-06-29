import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data class to represent a chat message
data class ChatMessage(val text: String, val isUser: Boolean)

// ViewModel for managing chat data and interactions
class ChatViewModel : ViewModel() {
    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    val _userInput = mutableStateOf("")
    val userInput: String by _userInput

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Generative model instance
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "AIzaSyDk7WJLgEmYCjVRB39DPjLTyhhR7kdQ5zM"
    )

    // Function to send a message
    fun sendMessage() {
        sendPromptToAI(userInput)
    }
    // Function to send a prompt to the AI model
    private fun sendPromptToAI(prompt: String) {
        _uiState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val chat = generativeModel.startChat()
            val response = chat.sendMessage(prompt)
            _messages.clear()
            _messages.add(ChatMessage(response.text!!, isUser = false))
            _messages.add(ChatMessage(userInput.trim(), isUser = true))
            _userInput.value = ""
            _uiState.value = UiState.Success
        }
    }
}

// UI State enum
sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
}

// Composable for the chat UI
@Composable
fun ChatScreen() {
    val viewModel: ChatViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Column to display the chat messages
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // LazyColumn to efficiently display the chat messages
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true // Display latest messages at the bottom
        ) {
            items(viewModel.messages) { message ->
                // Display each message with appropriate styling
                MessageItem(message)
            }
        }

        // Display loading indicator or error message based on UI state
        when (uiState) {
            is UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is UiState.Error -> {
                Text(
                    text = "Error: ${(uiState as UiState.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {} // Do nothing for other states
        }

        // Input field for the user's message
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text field for user input
            TextField(
                value = viewModel.userInput,
                onValueChange = { viewModel._userInput.value = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your message here...") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // Send button to send the message
            IconButton(onClick = { viewModel.sendMessage() }) {
                Icon(Icons.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

// Composable to display a single chat message
@Composable
fun MessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        // Text to display the message content
        Text(
            text = message.text,
            modifier = Modifier.padding(8.dp),
            color = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}


sealed class Content {
    data class Text(val text: String) : Content()
    // ... (other Content types like images, audio, etc.)
}