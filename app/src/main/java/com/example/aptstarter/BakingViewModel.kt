package com.example.aptstarter

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aptstarter.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class BakingViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()


    private val _uiStateImages: MutableStateFlow<UiStateImages> =
        MutableStateFlow(UiStateImages.Loading)
    var uiStateImages: StateFlow<UiStateImages> =
        _uiStateImages.asStateFlow()


    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-001",
        apiKey = BuildConfig.apiKey
    )

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.unsplash.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    fun sendPrompt(bitmap: Bitmap?, prompt: String?) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (bitmap != null && prompt != null) {
                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                response.text?.let { outputContent ->
                    _uiState.value = UiState.Success(outputContent)
                }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    fun sendPromptTitle(bitmap: Bitmap?, prompt: String?) {
        _uiState.value = UiState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (bitmap != null && prompt != null) {
                    val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                response.text?.let { outputContent ->
                    _uiState.value = UiState.SuccessTitle(outputContent)
                }
                    }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    fun getImagesFromApi() {
        val call = apiService.fetchData()
        Log.d("API CALL", "check this for api calls")
        _uiStateImages.value = UiStateImages.Loading
        call.enqueue(object : Callback<List<ApiResponse>> {
            override fun onResponse(call: Call<List<ApiResponse>>, response: Response<List<ApiResponse>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {
                        _uiStateImages.value = UiStateImages.SuccessImage(data)
                    }
                } else {
                    _uiStateImages.value = UiStateImages.Error("No more tokens.")
                    Log.d("API CALL", "No more tokens.")
                }
            }
            override fun onFailure(call: Call<List<ApiResponse>>, t: Throwable) {
                Log.d("API ERROR FORBIDDEN", t.toString())
            }
        })
    }
}

interface ApiService {
    @GET("photos/random/?client_id=HbE-YG8OvyLzO9oWSXyrTjqqgrwYlVkNMtbJmnX_D4Q&query=dessert&count=28")
    fun fetchData(
    ): Call<List<ApiResponse>> // ApiResponse es el modelo de datos esperado en la respuesta
}


data class ApiResponse(
    @SerializedName("id")
    val id: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("urls")
    val urls: Urls // Aquí usamos un objeto para representar las URLs
)

data class Urls(
    @SerializedName("raw")
    val raw: String,
    @SerializedName("full")
    val full: String,
    @SerializedName("regular")
    val regular: String,
    @SerializedName("small")
    val small: String,
    @SerializedName("thumb")
    val thumb: String,
    @SerializedName("small_s3")
    val smallS3: String
)

sealed interface UiStateImages {
    data class SuccessImage(val outputText: List<ApiResponse>) : UiStateImages
    data object Loading : UiStateImages
    data class Error(val errorMessage: String) : UiStateImages
}