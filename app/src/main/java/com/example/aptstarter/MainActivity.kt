package com.example.aptstarter
import ChatScreen
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aptstarter.R
import com.example.aptstarter.ui.theme.APTSTARTERTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            APTSTARTERTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    NavHost(navController = navController, startDestination = "first_screen") {
                        composable("first_screen") { BakingScreen() }
                        composable("second_screen") { ChatScreen() }
                    }
                }

                var checked by remember { mutableStateOf(true) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    Switch(
                        modifier = Modifier
                            .padding(bottom = 64.dp),
                        checked = checked,
                        onCheckedChange = {
                            checked = it
                        },
                        thumbContent = if (checked) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                                navController.navigate("first_screen")
                            }
                        } else {
                            navController.navigate("second_screen")
                            null
                        }
                    )

                    if (checked) {
                        SoundPlayer(rawResourceId = R.raw.audio)
                    }
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
}
