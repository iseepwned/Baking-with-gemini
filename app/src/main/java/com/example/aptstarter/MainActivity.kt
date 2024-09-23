package com.example.aptstarter

import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aptstarter.R
import com.example.aptstarter.ui.theme.APTSTARTERTheme
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener  {
    lateinit var tts: TextToSpeech
    var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle error: Language data is missing or the language is not supported.
            } else {
                isInitialized = true
            }
        } else {
            // Handle error: TextToSpeech initialization failed.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        tts.setSpeechRate(1.0f)  // Velocidad de habla
        tts.setPitch(1.0f)

        setContent {
            APTSTARTERTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NavHost(navController = navController, startDestination = "first_screen") {
                        composable("first_screen") { BakingScreen() }
                        composable("second_screen") {  ChatScreenWebSocket(navController = navController) }
                        composable("chat_general") {
                            ChatScreenWebSocket(navController = navController)
                        }
                        composable("personal_chat/{partnerName}/{randomName}/{requestAccepted}") { backStackEntry ->
                            val partnerName = backStackEntry.arguments?.getString("partnerName")
                            val randomName = backStackEntry.arguments?.getString("randomName")
                            val requestAccepted = backStackEntry.arguments?.getString("requestAccepted")
                            PersonalChatScreen(nameRandom = randomName ?: "" ,partnerName ?: "", requestAccepted = requestAccepted ?: "", navController = navController)
                        }
                    }
                }

                var checked by remember { mutableStateOf(true) }
                var switchPosition by remember { mutableStateOf(Position(0f, 0f)) }

                Switch(
                    modifier = Modifier
                        .offset { IntOffset(switchPosition.x.toInt(), switchPosition.y.toInt()) }
                        .graphicsLayer {
                            translationX = switchPosition.x
                            translationY = switchPosition.y
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                switchPosition = Position(
                                    switchPosition.x + dragAmount.x,
                                    switchPosition.y + dragAmount.y
                                )
                            }
                        },
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        if (checked) {
                            navController.navigate("first_screen")
                        } else {
                            navController.navigate("second_screen")
                        }
                    },
                    thumbContent = {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                )

                if (checked) {
                    SoundPlayer(rawResourceId = R.raw.audio)
                }

            }
        }
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

    data class Position(val x: Float, val y: Float)
}




