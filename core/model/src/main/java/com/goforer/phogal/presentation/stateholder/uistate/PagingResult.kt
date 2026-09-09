package com.goforer.phogal.presentation.stateholder.uistate

/**
 * Encapsulates the loading state of a paging operation for UI consumption.
 */
sealed interface PagingResult {
    data object Loading : PagingResult
    data class Success(val message: String) : PagingResult
    data class Error(val error: ErrorEntity) : PagingResult
}
