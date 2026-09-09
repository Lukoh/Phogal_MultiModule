package com.goforer.phogal.presentation.stateholder.uistate

/**
 * Structured representation of errors that can occur in the application.
 * Categorized by the source of the error to allow for specific UI branching.
 */
sealed interface ErrorEntity {
    val message: String

    /** Network/API related errors, typically wrapping HTTP codes. */
    data class Network(val code: Int, override val message: String) : ErrorEntity

    /** Persistence related errors (e.g., Room, DataStore). */
    data class Persistence(override val message: String) : ErrorEntity

    /** Permission related errors. */
    data class Permission(override val message: String) : ErrorEntity

    /** Catch-all for unexpected failures. */
    data class Unknown(override val message: String) : ErrorEntity
}
