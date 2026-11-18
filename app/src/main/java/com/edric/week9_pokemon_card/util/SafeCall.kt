package com.edric.week9_pokemon_card.util

import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

/**
 * Pengganti runCatching untuk coroutine: CancellationException WAJIB dilempar ulang,
 * agar pembatalan coroutine (mis. ViewModel dibuang atau search baru dimulai) tidak ikut tertelan.
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

/** Mengubah exception teknis menjadi pesan singkat untuk UI error state. */
fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> "HTTP ${code()}"
    is IOException -> "No internet connection"
    else -> message ?: "Unknown error"
}
