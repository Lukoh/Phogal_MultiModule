package com.goforer.phogal.data.datasource.network

import com.goforer.phogal.data.datasource.network.NetworkResult.Empty
import com.goforer.phogal.data.datasource.network.NetworkResult.Error
import com.goforer.phogal.data.datasource.network.NetworkResult.Exception
import com.goforer.phogal.data.datasource.network.NetworkResult.Success

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

/** Convenience: is this a successful state? */
val NetworkResult<*>.isSuccess: Boolean
    get() = this is Success

/** Convenience: is this a terminal failure state (either [Error] or [Exception])? */
val NetworkResult<*>.isFailure: Boolean
    get() = this is Error || this is Exception

/** Returns the data if this is [Success], null otherwise. */
fun <T> NetworkResult<T>.getOrNull(): T? = (this as? Success)?.data

/** Returns the data if this is [Success], or [defaultValue] otherwise. */
fun <T> NetworkResult<T>.getOrElse(defaultValue: () -> T): T =
    (this as? Success)?.data ?: defaultValue()

/** Returns the throwable if this is [Exception], null otherwise. */
fun NetworkResult<*>.exceptionOrNull(): Throwable? =
    (this as? Exception)?.throwable

/**
 * Performs the given [action] if this is [Success].
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is Success) action(data)
    return this
}

/**
 * Performs the given [action] if this is [Empty].
 */
inline fun <T> NetworkResult<T>.onEmpty(action: () -> Unit): NetworkResult<T> {
    if (this is Empty) action()
    return this
}

/**
 * Performs the given [action] if this is [Error].
 */
inline fun <T> NetworkResult<T>.onError(action: (code: Int, message: String) -> Unit): NetworkResult<T> {
    if (this is Error) action(code, message)
    return this
}

/**
 * Performs the given [action] if this is [Exception].
 */
inline fun <T> NetworkResult<T>.onException(action: (Throwable) -> Unit): NetworkResult<T> {
    if (this is Exception) action(throwable)
    return this
}

/**
 * Performs the given [action] if this is either [Error] or [Exception].
 */
inline fun <T> NetworkResult<T>.onFailure(action: (message: String?, throwable: Throwable?) -> Unit): NetworkResult<T> {
    when (this) {
        is Error -> action(message, null)
        is Exception -> action(null, throwable)
        else -> Unit
    }
    return this
}

/**
 * Maps the success payload of a [NetworkResult] to another type, leaving failure states untouched.
 */
inline fun <T, R> NetworkResult<T>.mapSuccess(transform: (T) -> R): NetworkResult<R> = when (this) {
    is Success -> Success(transform(data))
    is Empty -> Empty
    is Error -> this
    is Exception -> this
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
    is Success -> onSuccess(data)
    is Empty -> onEmpty()
    is Error -> onError(code, message)
    is Exception -> onException(throwable)
}
