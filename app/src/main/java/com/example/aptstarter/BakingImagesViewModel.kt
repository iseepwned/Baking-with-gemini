package com.example.aptstarter

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aptstarter.UiState
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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