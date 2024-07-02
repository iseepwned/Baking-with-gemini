package com.example.aptstarter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.aptstarter.R
import kotlinx.coroutines.launch

data class Topic(val name: String, val description: String)


var descriptions by mutableStateOf<List<String>>(emptyList())
var urls by mutableStateOf<List<Urls>>(emptyList())
var listImages by mutableStateOf<List<ApiResponse>>(emptyList())
var bitmap by mutableStateOf<Bitmap?>(null)
var prompt by mutableStateOf("")
var result by mutableStateOf("")

@OptIn(ExperimentalCoilApi::class, ExperimentalFoundationApi::class)
@Composable
@Preview
fun BakingScreen(
    bakingViewModel: BakingViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by bakingViewModel.uiState.collectAsState()
    val uiStateImages by bakingViewModel.uiStateImages.collectAsState()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            bakingViewModel.getImagesFromApi()
        }
    }

    // Guardar las imágenes y descripciones una vez cargadas por primera vez
    if (uiStateImages is UiStateImages.SuccessImage && listImages.isEmpty()) {
        listImages = (uiStateImages as UiStateImages.SuccessImage).outputText
        descriptions = listImages.map { it.description ?: "Promote your image site here. \n\n\n https://www.linkedin.com/in/facundoesteban9/ " }
        urls = listImages.map { it.urls }
    } else if (uiStateImages is UiStateImages.Error) {
        println("Error: ${(uiStateImages as UiStateImages.Error).errorMessage}")
    }

    // Column principal con desplazamiento vertical
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Solo un uso correcto de verticalScroll aquí
    ) {
        // Contenido del HorizontalPager
        val pagerState = rememberPagerState(
            pageCount = { urls.size },
            initialPage = 0
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) { page ->
            val isSelected = page == pagerState.currentPage
            val url = urls[page]
            val painter = rememberImagePainter(
                data = url.raw,
                builder = {
                    crossfade(800)
                }
            )


            LaunchedEffect(painter) {
                snapshotFlow { painter.state }
                    .collect { state ->
                        if (state is ImagePainter.State.Success) {
                            bitmap = state.result.drawable.toBitmap()
                            println(bitmap)
                        }
                    }
            }

            // Botón para marcar una idea relacionada con la imagen
            var showDialog by remember { mutableStateOf(false) }
            var markedIdea by remember { mutableStateOf(descriptions[pagerState.currentPage]) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .height(400.dp)
                    .align(Alignment.CenterHorizontally)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                width = 4.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (painter.state is ImagePainter.State.Loading) {
                    CircularProgressIndicator()
                } else {
                    var zoomScale by remember { mutableStateOf(1f) }

                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .graphicsLayer(
                                scaleX = zoomScale,
                                scaleY = zoomScale,
                                translationX = 0f, // Ajusta según sea necesario
                                translationY = 0f, // Ajusta según sea necesario
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        showDialog = true
                                    }
                                )
                            },
                        contentScale = ContentScale.Crop
                    )
                }

                IconButton(
                    onClick = {
                        markedIdea = descriptions[pagerState.currentPage]
                        showDialog = true
                    },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_question_mark_24),
                        contentDescription = "Icono Info",
                        modifier = Modifier.requiredSize(28.dp)
                    )
                }

                // Diálogo para ingresar la idea marcada
                if (showDialog) {
                    Dialog(
                        onDismissRequest = { showDialog = false }
                    ) {
                        Card(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = markedIdea,
                                    modifier = Modifier.padding(16.dp)
                                )
                                BasicTextField(
                                    value = "",
                                    onValueChange = { },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                )
                                Button(
                                    onClick = {
                                        // Aquí podrías enviar la idea marcada al ViewModel si es necesario
                                        showDialog = false
                                    },
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(16.dp)
                                ) {
                                    Text("Aceptar")
                                }
                            }
                        }
                    }
                }
            }
        }

        @Composable
        fun ScrollableButtons() {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val topics = listOf(
                    Topic("Recipe Details", "Give me a good new question about this image & topic."),
                    Topic("Flavor Profile", "Inquire about the taste, texture, and overall flavor."),
                    Topic("Presentation Techniques", "Discuss techniques used to present food attractively."),
                    Topic("Inspiration Sources", "Explore where the inspiration for the dish came from."),
                    Topic("Special Occasions", "Ask if the dish is prepared for a special event or celebration."),
                    Topic("Cooking Tips", "Seek advice or tips for cooking similar dishes."),
                    Topic("Ingredients Used", "Inquire about the specific ingredients used in the dish."),
                    Topic("Personal Experiences", "Discuss personal experiences related to preparing or eating the dish."),
                    Topic("Cultural Significance", "Explore any cultural or traditional aspects of the dish."),
                    Topic("Variations and Alternatives", "Discuss alternative versions or variations of the dish.")
                )

                items(topics.size) { index ->
                    Button(
                        onClick = { /* bakingViewModel.sendPromptTitle(bitmap, topics[index].description + topics[index].description)*/
                            prompt =  topics[index].description
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(text = topics[index].name)
                    }
                }

            }
        }

        ScrollableButtons()

        Row() {
            TextField(
                value = prompt,
                placeholder = { Text("Ask something...") },
                onValueChange = { prompt = it },
                modifier = Modifier
                    .weight(0.9f)
                    .align(Alignment.CenterVertically)
                    .padding(8.dp)
            )
            val keyboardController = LocalSoftwareKeyboardController.current
            IconButton(
                onClick = {
                    keyboardController?.hide()
                    if (urls.isNotEmpty() && bitmap != null) {
                        bitmap.let { bakingViewModel.sendPrompt(bitmap!!, prompt) }
                    }
                    prompt = ""
                },
                enabled = urls.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.CenterVertically)

            ) {
                Image(
                    painter = painterResource(id = R.drawable.gemini),
                    contentDescription = "Execute",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (uiState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp))
        } else {
            var textColor = MaterialTheme.colorScheme.onSurface
            result = when (uiState) {
                is UiState.Error -> {
                    textColor = MaterialTheme.colorScheme.error
                    (uiState as UiState.Error).errorMessage
                }
                is UiState.Success -> {
                    textColor = MaterialTheme.colorScheme.onSurface
                    (uiState as UiState.Success).outputText
                }
                is UiState.SuccessTitle -> {
                    prompt = (uiState as UiState.SuccessTitle).outputText
                    ""
                }
                else -> result
            } 

            Text(result, style = MaterialTheme.typography.bodyLarge.copy(color = textColor), modifier = Modifier
                .padding(16.dp)
                .fillMaxSize())

            // Botón con ícono de información para copiar al portapapeles
            IconButtonWithCopyToClipboard(
                onClick = { /* Acción adicional si es necesaria al hacer clic */ },
                resultToCopy = result
            )
        }
    }
}

@Composable
fun IconButtonWithCopyToClipboard(
    onClick: () -> Unit,
    resultToCopy: String,
    modifier: Modifier = Modifier,
    painter: Painter = painterResource(id = R.drawable.copy_foreground),
    contentDescription: String = "Icono Info"
) {
    val context = LocalContext.current

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        IconButton(
            onClick = {
                onClick() // Llama a la acción principal (si la hubiera) cuando se hace clic en el botón
                // Copia el resultado al portapapeles
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Find me:", "https://www.linkedin.com/in/facundoesteban9/"))
            },
        ) {
            Image(
                painter = painterResource(id = R.mipmap.linkedin),
                contentDescription = contentDescription,
                modifier = Modifier.requiredSize(32.dp)
            )
        }
        IconButton(
            onClick = {
                onClick() // Llama a la acción principal (si la hubiera) cuando se hace clic en el botón
                // Copia el resultado al portapapeles
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Copied Text", resultToCopy))
            },
        ) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.requiredSize(28.dp)
            )
        }
    }
}
