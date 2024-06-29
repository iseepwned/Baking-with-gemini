package com.example.aptstarter


import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.aptstarter.R
import com.aptstarter.UiState

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalCoilApi::class)

@Composable
fun BoxWithBackgroundImage(painter: Painter) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray) // Color de fondo de respaldo mientras se carga la imagen
        ) {
            Image(
                painter = painter,
                contentDescription = null, // Descripción opcional de la imagen
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds // Escala de contenido de la imagen
            )
        }
    }
}

@Composable
fun BakingScreen(
    bakingViewModel: BakingViewModel = viewModel()
) {
    val selectedImage = remember { mutableIntStateOf(0) }
    val placeholderPrompt = ""
    val placeholderResult = stringResource(R.string.results_placeholder)
    var prompt by rememberSaveable { mutableStateOf(placeholderPrompt) }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    var urls: List<Urls> = mutableListOf()

    val uiState by bakingViewModel.uiState.collectAsState()

    // Llamar getImagesFromApi() solo una vez al iniciar el composable
    LaunchedEffect(Unit) {
        bakingViewModel.getImagesFromApi()
    }


    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth()
        )  {

            itemsIndexed(urls) { index, url ->
                val painter = rememberImagePainter(
                    data = url.full,
                    builder = {
                        transformations(CircleCropTransformation())
                    }
                )

                val isSelected = index == selectedImage.intValue

                var bitmap by remember(url) { mutableStateOf<Bitmap?>(null) }

                // Observe the painter state to get the Bitmap
                LaunchedEffect(painter) {
                    snapshotFlow { painter.state }
                        .collect { state ->
                            if (state is ImagePainter.State.Success) {
                                bitmap = state.result.drawable.toBitmap()
                            }
                        }
                }

                // Use the bitmap for further processing if needed
                bitmap?.let {
                    url.bitmap = it // Store bitmap in your Url object if necessary
                    println("Bitmap saved: ${url.bitmap}")
                }

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .requiredSize(200.dp)
                        .clickable { selectedImage.intValue = index }
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    BorderStroke(
                                        4.dp,
                                        MaterialTheme.colorScheme.primary
                                    )
                                )
                            } else {
                                Modifier
                            }
                        ),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Row(
            modifier = Modifier.padding(all = 16.dp)
        ) {

            TextField(
                value = prompt,
                label = { Text(stringResource(R.string.label_prompt)) },
                onValueChange = {
                    prompt = it },
                modifier = Modifier
                    .weight(0.8f)
                    .padding(end = 16.dp)
                    .align(Alignment.CenterVertically)
            )

            Button(
                onClick = {
                    if (urls.isNotEmpty()) {
                        bakingViewModel.sendPrompt(urls[selectedImage.intValue].bitmap, prompt)
                    }
                },
                enabled = prompt.isNotEmpty(),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                Text(text = stringResource(R.string.action_go))
            }
        }

        if (uiState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            var textColor = MaterialTheme.colorScheme.onSurface
            if (uiState is UiState.Error) {
                textColor = MaterialTheme.colorScheme.error
                result = (uiState as UiState.Error).errorMessage
            } else if (uiState is UiState.Success) {
                textColor = MaterialTheme.colorScheme.onSurface
                result = (uiState as UiState.Success).outputText
            } else if (uiState is UiState.SuccessTitle) {
                prompt = (uiState as UiState.SuccessTitle).outputText
            } else if (uiState is UiState.SuccessImage) {
                urls = (uiState as UiState.SuccessImage).outputText

            }
            val scrollState = rememberScrollState()
            Text(
                text = result,
                textAlign = TextAlign.Start,
                color = textColor,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            )
        }
    }
}