package com.aptstarter

import com.example.aptstarter.Urls

/**
 * A sealed hierarchy describing the state of the text generation.
 */
sealed interface UiState {

    /**
     * Empty state when the screen is first shown
     */
    object Initial : UiState

    /**
     * Still loading
     */
    object Loading : UiState

    /**
     * Text has been generated
     */

    data class SuccessImage(val outputText: List<Urls>) : UiState

    data class SuccessTitle(val outputText: String) : UiState

    data class Success(val outputText: String) : UiState

    /**
     * There was an error generating text
     */
    data class Error(val errorMessage: String) : UiState
}