package com.goforer.phogal.data.model

/**
 * Exception thrown when a backend error occurs (HTTP 4xx/5xx).
 */
class BackendException(val code: Int, override val message: String) : Exception(message)
