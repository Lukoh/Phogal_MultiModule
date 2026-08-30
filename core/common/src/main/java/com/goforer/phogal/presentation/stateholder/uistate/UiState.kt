package com.goforer.phogal.presentation.stateholder.uistate

/**
 * A generic, type-safe UI state container for ViewModel → Compose consumption.
 *
 * Replaces the legacy `Resource` / `StateFlow<Any>` pattern. In Compose UI,
 * collect the owning `StateFlow<UiState<T>>` once with `collectAsStateWithLifecycle()`
 * and branch with an exhaustive `when`:
 *
 * ```
 * when (val state = viewModel.uiState.collectAsStateWithLifecycle().value) {
 *     UiState.Idle     -> { /* initial, no request yet */ }
 *     UiState.Loading  -> LoadingIndicator()
 *     is UiState.Success -> Content(state.data)
 *     is UiState.Error   -> ErrorContent(state.code, state.message, onRetry = ...)
 * }
 * ```
 *
 * Note: this type is intentionally UI-layer only. Repositories should return
 * [com.goforer.phogal.data.datasource.network.NetworkResult]; ViewModels translate
 * that into [UiState] before exposing it to Compose.
 */
sealed interface UiState<out T> {

    /** No request has been made yet. Typical initial value for a `StateFlow`. */
    data object Idle : UiState<Nothing>

    /** A request is in flight; UI should show a loading placeholder. */
    data object Loading : UiState<Nothing>

    /** A request succeeded with a value. */
    data class Success<T>(val data: T) : UiState<T>

    /**
     * A request failed. [code] is the HTTP status code when known (0 when the failure
     * happened before a response — e.g. an [java.io.IOException]).
     */
    data class Error(
        val code: Int,
        val message: String
    ) : UiState<Nothing>
}

object UIConstants {
    const val UP_BUTTON_THRESHOLD = 4
    const val SCROLL_OFFSET_SIGNAL = 35
    /** Matches `UserPhotosViewModel.PAGE_SIZE`. Kept local so the section stays decoupled. */
    const val PAGE_SIZE_HINT = 10
}
