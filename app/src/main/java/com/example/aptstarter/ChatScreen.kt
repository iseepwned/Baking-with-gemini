
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aptstarter.BuildConfig
import com.aptstarter.R
import com.example.aptstarter.MainActivity
import com.example.aptstarter.room.RoomDB
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data class to represent a chat message
data class ChatMessage(val text: String, val isUser: Boolean)
private val _messages = mutableStateListOf<ChatMessage>()
private var listHistory  =   mutableStateListOf<ChatMessage>()
// ViewModel for managing chat data and interactions
class ChatViewModel : ViewModel() {

    val messages: List<ChatMessage> = _messages

    val _userInput = mutableStateOf("")
    val userInput: String by _userInput

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Generative model instance
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    // Function to send a message
    fun sendMessage(history: MutableList<ChatMessage>) {
        sendPromptToAI(userInput, history)
    }

    private fun sendPromptToAI(prompt: String, history: MutableList<ChatMessage>) {
        _uiState.value = UiState.Loading

        val formattedMessages = mutableListOf<com.google.ai.client.generativeai.type.Content>()

        // Format history messages
        for (message in history) {
            val content = when {
                message.isUser -> content(role = "user") { text(message.text) }
                else -> content(role = "model") { text(message.text) }
            }
            formattedMessages.add(content)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val chat = generativeModel.startChat(formattedMessages)

            // Send user prompt
            val response = chat.sendMessage(prompt)

            // Update messages
            _messages.add(ChatMessage(response.text ?: "Try again.", isUser = false))
            _messages.add(ChatMessage(prompt, isUser = true))
            history.add(ChatMessage(response.text ?: "Try again.", isUser = false))
            history.add(ChatMessage(prompt, isUser = true))

            // Update UI state
            _userInput.value = ""
            _uiState.value = UiState.Success(history)
        }
    }
}

// UI State enum
sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    class Success(val history: MutableList<ChatMessage>) : UiState()
    data class Error(val message: String) : UiState()
}

@Composable
fun SoundPlayer(rawResourceId: Int) {
    val context = LocalContext.current
    var mediaPlayer by remember { mutableStateOf(MediaPlayer.create(context, rawResourceId)) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }
    mediaPlayer.start()
}

@Composable
fun ChatScreen() {
    val viewModel: ChatViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val roomDB = remember { mutableStateOf<RoomDB?>(null) }

    // Inicialización asincrónica de la base de datos usando LaunchedEffect
    LaunchedEffect(Unit) {
        roomDB.value = RoomDB.getDatabase(context)
    }

    // Estado para almacenar la lista de historiales
    val historyList = remember(roomDB.value) {
        roomDB.value?.historyDao()?.getAll() ?: emptyList()
    }
    // Column to display the chat messages
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.neww),
            contentDescription = "Assistant",
            modifier = Modifier.requiredSize(38.dp)
        )
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
                _messages.clear()
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom=64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Text(
                    text = "Error: ${(uiState as UiState.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {
                //(LocalContext.current as MainActivity).historyDao.insert((uiState as UiState.Success).history)
            }
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
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp),
                placeholder = { Text("Write here...") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // Send button to send the message
            IconButton(onClick = { viewModel.sendMessage(listHistory) }) {
                Icon(Icons.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

// Composable to display a single chat message
@Composable
fun MessageItem(message: ChatMessage) {
    if (!message.isUser) {
        SpeechSynthesisApp(message.text)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        // Text to display the message content
        SelectionContainer{
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp),
                color = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        if (!message.isUser) {
            SoundPlayer(rawResourceId = (R.raw.result))
        }
    }
}

/*@Composable
fun DotsLoadingIndicator(modifier: Modifier = Modifier) {
    var dotOffset by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val dotAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    LaunchedEffect(key1 = dotAnimation) {
        dotOffset = dotAnimation
    }

    Canvas(modifier = modifier.fillMaxWidth()) {
        val numDots = 12
        val dotSize = 10f
        val center = size / 2f

        for (i in 0 until numDots) {
            val offsetAngle = i * (360 / numDots)
            val dotAngle = offsetAngle + (dotOffset * 60)
            val dotX =  (size.width / 4 * cos(Math.toRadians(dotAngle.toDouble()))).toFloat()
            val dotY =  (size.width / 4 * sin(Math.toRadians(dotAngle.toDouble()))).toFloat()

            drawCircle(
                color = Color.White,
                radius = dotSize,
                center = androidx.compose.ui.geometry.Offset(dotX, dotY),
                style = Stroke(dotSize)
            )
        }
    }
}*/

/*@Composable
fun DotsLoadingIndicatorPreview() {
    Box(
        modifier = Modifier.fillMaxSize()
        .padding(start = 200.dp),
        contentAlignment = Alignment.Center
    ) {
        DotsLoadingIndicator()
    }
}*/

@Composable
fun SpeechSynthesisApp(message: String) {
    val context = LocalContext.current
    var textToSpeak by remember { mutableStateOf(message) }

    IconButton(
        onClick = {
            if ((context as? MainActivity)?.isInitialized == true) {
                context.tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        },

        ) {
        Image(
            painter = painterResource(id = R.drawable.playbutton_foreground),
            contentDescription = "Execute",
            modifier = Modifier.requiredSize(52.dp)
        )
    }
}

sealed class Content {
    data class Text(val text: String) : Content()
    // ... (other Content types like images, audio, etc.)
}