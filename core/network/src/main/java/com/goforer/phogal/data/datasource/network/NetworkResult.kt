package com.goforer.phogal.data.datasource.network

/**
 * Type-safe result wrapper for network responses.
 *
 * Replaces the legacy mutable `Resource` class. A `NetworkResult` is one of:
 *  - [Success]   : HTTP 2xx with a non-null body
 *  - [Empty]     : HTTP 204 / empty body (successful, no payload)
 *  - [Error]     : HTTP 4xx/5xx with status code and error message
 *  - [Exception] : Network / IO / serialization failure before any HTTP response was produced
 *
 * Consumers should prefer exhaustive `when` over null checks or `is` casts to `Any`.
 */
sealed interface NetworkResult<out T> {

    data class Success<T>(val data: T) : NetworkResult<T>

    data object Empty : NetworkResult<Nothing>

    data class Error(
        val code: Int,
        val message: String
    ) : NetworkResult<Nothing>

    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>
}

/**
 * Exception thrown when a backend error occurs (HTTP 4xx/5xx).
 */
class BackendException(val code: Int, override val message: String) : Exception(message)

/** Convenience: is this a successful state? */
val NetworkResult<*>.isSuccess: Boolean
    get() = this is NetworkResult.Success

/** Convenience: is this a terminal failure state (either [Error] or [Exception])? */
val NetworkResult<*>.isFailure: Boolean
    get() = this is NetworkResult.Error || this is NetworkResult.Exception

/** Returns the data if this is [Success], null otherwise. */
fun <T> NetworkResult<T>.getOrNull(): T? = (this as? NetworkResult.Success)?.data

/** Returns the data if this is [Success], or [defaultValue] otherwise. */
fun <T> NetworkResult<T>.getOrElse(defaultValue: () -> T): T =
    (this as? NetworkResult.Success)?.data ?: defaultValue()

/** Returns the throwable if this is [Exception], null otherwise. */
fun NetworkResult<*>.exceptionOrNull(): Throwable? =
    (this as? NetworkResult.Exception)?.throwable

/**
 * Performs the given [action] if this is [Success].
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) action(data)
    return this
}

/**
 * Performs the given [action] if this is [Empty].
 */
inline fun <T> NetworkResult<T>.onEmpty(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Empty) action()
    return this
}

/**
 * Performs the given [action] if this is [Error].
 */
inline fun <T> NetworkResult<T>.onError(action: (code: Int, message: String) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) action(code, message)
    return this
}

/**
 * Performs the given [action] if this is [Exception].
 */
inline fun <T> NetworkResult<T>.onException(action: (Throwable) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Exception) action(throwable)
    return this
}

/**
 * Performs the given [action] if this is either [Error] or [Exception].
 */
inline fun <T> NetworkResult<T>.onFailure(action: (message: String?, throwable: Throwable?) -> Unit): NetworkResult<T> {
    when (this) {
        is NetworkResult.Error -> action(message, null)
        is NetworkResult.Exception -> action(null, throwable)
        else -> Unit
    }
    return this
}

/**
 * Maps the success payload of a [NetworkResult] to another type, leaving failure states untouched.
 */
inline fun <T, R> NetworkResult<T>.mapSuccess(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data))
    is NetworkResult.Empty -> NetworkResult.Empty
    is NetworkResult.Error -> this
    is NetworkResult.Exception -> this
}

/**
 * Handles all possible states of a [NetworkResult] in a single functional call.
 */
inline fun <T, R> NetworkResult<T>.fold(
    onSuccess: (T) -> R,
    onEmpty: () -> R,
    onError: (code: Int, message: String) -> R,
    onException: (Throwable) -> R
): R = when (this) {
    is NetworkResult.Success -> onSuccess(data)
    is NetworkResult.Empty -> onEmpty()
    is NetworkResult.Error -> onError(code, message)
    is NetworkResult.Exception -> onException(throwable)
}
