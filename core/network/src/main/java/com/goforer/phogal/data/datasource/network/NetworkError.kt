package com.goforer.phogal.data.datasource.network

import kotlinx.serialization.Serializable

@Serializable
data class NetworkError(val detail: List<ErrorBody>) {

    /**
     * One error entry. The Unsplash API returns at least one entry per error
     * response. `msg` is `var` because the request interceptor in
     * `NetworkModule.provideRequestInterceptor` mutates it to prepend the URL path
     * — that behavior pre-dates this migration and is preserved as-is.
     */
    @Serializable
    data class ErrorBody(
        val loc: List<String>,
        var msg: String = ERROR_DEFAULT,
        val type: String
    )

    companion object {
        const val ERROR_DEFAULT = "An unexpected error has occurred"

        const val ERROR_SERVICE_UNAVAILABLE = 503
        const val ERROR_SERVICE_BAD_GATEWAY = 502

        const val ERROR_SERVICE_UNPROCESSABLE_ENTITY = 422
    }
}
