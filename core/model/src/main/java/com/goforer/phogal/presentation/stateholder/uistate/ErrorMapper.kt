package com.goforer.phogal.presentation.stateholder.uistate

import com.goforer.phogal.data.model.BackendException
import java.io.IOException

/**
 * Maps a [Throwable] to a structured [ErrorEntity].
 */
fun Throwable.toErrorEntity(): ErrorEntity {
    return when (this) {
        is BackendException -> ErrorEntity.Network(code, message ?: "Unknown Network Error")
        is IOException -> ErrorEntity.Network(0, message ?: "Network connection error")
        // Note: Specific exceptions for Persistence or Permission can be added here
        // as the app's requirements grow.
        else -> ErrorEntity.Unknown(message ?: "An unexpected error occurred")
    }
}
