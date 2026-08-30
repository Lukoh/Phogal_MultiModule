package com.goforer.phogal.data.datasource.network

import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles parsing and processing of network errors.
 * 
 * This class provides utilities to decode the [NetworkError] DTO from raw
 * response bodies and can be used to perform global side effects based on
 * specific error types (e.g., logging out on session expiry).
 */
@Singleton
class NetworkErrorHandler @Inject constructor(
    private val json: Json
) {
    /**
     * Decodes a [NetworkError] from the provided JSON string.
     * 
     * @param jsonString The raw error body from the network response.
     * @return The parsed [NetworkError], or null if parsing fails.
     */
    fun parseError(jsonString: String?): NetworkError? {
        if (jsonString.isNullOrBlank()) return null

        return runCatching {
            json.decodeFromString<NetworkError>(jsonString)
        }.onFailure { e ->
            Timber.w(e, "Failed to parse NetworkError JSON")
        }.getOrNull()
    }

    /**
     * Processes an error message and performs side effects if necessary.
     * 
     * @param errorMessage The raw JSON error string.
     * @return A human-readable message extracted from the error body.
     */
    fun getErrorMessage(errorMessage: String?): String {
        val error = parseError(errorMessage) ?: return "Unknown network error"
        
        // Handle specific error types (side effects)
        error.detail.firstOrNull()?.type?.let { type ->
            when (type) {
                "INVALID_SESSION" -> {
                    Timber.e("Session expired. Trigger logout.")
                    // TODO: Trigger global logout event via a SharedFlow or similar.
                }
                "OBSOLETE_VERSION" -> {
                    Timber.e("App version is obsolete. Prompt for update.")
                }
            }
        }

        return error.detail.firstOrNull()?.msg ?: "Unexpected error occurred"
    }
}
