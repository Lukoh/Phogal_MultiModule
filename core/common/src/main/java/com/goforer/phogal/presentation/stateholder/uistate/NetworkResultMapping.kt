package com.goforer.phogal.presentation.stateholder.uistate

import com.goforer.phogal.data.datasource.network.NetworkResult

/**
 * Converts a data-layer [NetworkResult] into a UI-layer [UiState].
 *
 * [NetworkResult.Empty] is collapsed into an [UiState.Success] that carries `null`
 * as its payload — callers for endpoints that can legitimately return empty bodies
 * (e.g. HTTP 204 on a `POST .../like`) should use this overload. For endpoints that
 * must always return a body (e.g. `GET .../photos/{id}`), prefer the non-null mapper.
 * Non-null variant: use when an [NetworkResult.Empty] should be treated as an error
 * (the server broke its contract by returning an empty body).
 */
fun <T> NetworkResult<T>.toUiState(): UiState<T> = when (this) {
    is NetworkResult.Success -> UiState.Success(data)
    is NetworkResult.Empty      -> UiState.Error(ErrorEntity.Network(204, "Empty response body"))
    is NetworkResult.Error   -> UiState.Error(ErrorEntity.Network(code, message))
    is NetworkResult.Exception -> UiState.Error(throwable.toErrorEntity())
}
