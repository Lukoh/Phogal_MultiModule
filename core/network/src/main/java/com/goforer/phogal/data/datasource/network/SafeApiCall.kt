package com.goforer.phogal.data.datasource.network

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Executes a Retrofit suspend call and maps the result into a [NetworkResult].
 *
 * Rules:
 *  - HTTP 204 or a null body on a 2xx response produces [NetworkResult.Empty].
 *  - HTTP 2xx with a non-null body produces [NetworkResult.Success].
 *  - HTTP non-2xx produces [NetworkResult.Error] with code + best-effort error message.
 *  - [IOException] (connectivity, timeouts) produces [NetworkResult.Exception].
 *  - [SerializationException] (JSON parsing errors) produces [NetworkResult.Exception].
 *  - Any other thrown [Throwable] also produces [NetworkResult.Exception].
 *  - [CancellationException] is re-thrown so structured concurrency keeps working;
 *    it is never swallowed.
 */
suspend inline fun <T> safeApiCall(
    crossinline block: suspend () -> Response<T>
): NetworkResult<T> {
    return try {
        val response = block()
        if (response.isSuccessful) {
            val body = response.body()
            if (body == null || response.code() == 204) {
                NetworkResult.Empty
            } else {
                NetworkResult.Success(body)
            }
        } else {
            val code = response.code()
            val errorMessage = response.extractErrorMessage()
            Timber.w("safeApiCall HTTP $code: $errorMessage")
            NetworkResult.Error(code = code, message = errorMessage)
        }
    } catch (ce: CancellationException) {
        // Essential for Coroutines to handle cancellation correctly.
        throw ce
    } catch (se: SerializationException) {
        Timber.e(se, "safeApiCall SerializationException - JSON mismatch")
        NetworkResult.Exception(se)
    } catch (ioe: IOException) {
        Timber.w(ioe, "safeApiCall IOException - Connectivity/Timeout")
        NetworkResult.Exception(ioe)
    } catch (he: HttpException) {
        val code = he.code()
        val message = he.message() ?: "HTTP $code"
        Timber.w(he, "safeApiCall HttpException: $code")
        NetworkResult.Error(code = code, message = message)
    } catch (t: Throwable) {
        Timber.e(t, "safeApiCall unexpected error")
        NetworkResult.Exception(t)
    }
}

/**
 * Attempts to extract a human-readable error message from a Retrofit [Response].
 * It looks at the error body, falling back to the response message, and finally
 * a generic string.
 */
@PublishedApi
internal fun Response<*>.extractErrorMessage(): String {
    return runCatching { errorBody()?.string() }.getOrNull()
        ?.takeIf { it.isNotBlank() }
        ?: message()
        ?: "Unknown HTTP error"
}
