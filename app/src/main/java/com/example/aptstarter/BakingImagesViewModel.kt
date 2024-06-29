package com.example.aptstarter

import androidx.lifecycle.ViewModel

class BakingImagesViewModel : ViewModel()  {
  /*  private fun getImagesViewModel(){
        fun getImages(
            bitmap: Bitmap,
            prompt: String
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val response = generativeModel.(
                            content {
                                image(bitmap)
                                text(prompt)
                            }
                            )
                    response.text?.let { outputContent ->
                        _uiState.value = UiState.Success(outputContent)
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(e.localizedMessage ?: "")
                }
            }
        }
    }*/
}