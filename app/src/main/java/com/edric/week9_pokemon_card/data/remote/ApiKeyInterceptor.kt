package com.edric.week9_pokemon_card.data.remote

import java.util.concurrent.atomic.AtomicInteger
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Menambahkan header `Authorization: Bearer <API_KEY>` ke setiap request.
 * Mendukung banyak API key (listOf): jika key saat ini terkena limit (429) atau ditolak (401/403),
 * interceptor otomatis mencoba key berikutnya dalam daftar.
 */
class ApiKeyInterceptor(private val apiKeys: List<String>) : Interceptor {

    private val currentKeyIndex = AtomicInteger(0)

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val activeKeys = apiKeys.filter { it.isNotBlank() }

        if (activeKeys.isEmpty()) {
            return chain.proceed(original)
        }

        val startIndex = currentKeyIndex.get().mod(activeKeys.size)
        var lastResponse: Response? = null

        for (attempt in activeKeys.indices) {
            val keyIndex = (startIndex + attempt) % activeKeys.size
            val apiKey = activeKeys[keyIndex]

            lastResponse?.close()
            val authenticatedRequest = original.newBuilder()
                .header("Authorization", "Bearer $apiKey")
                .build()

            val response = chain.proceed(authenticatedRequest)
            if (response.code !in RETRYABLE_AUTH_OR_QUOTA_CODES || attempt == activeKeys.lastIndex) {
                currentKeyIndex.set(keyIndex)
                return response
            }

            // Quota habis / key ditolak: pindah ke key berikutnya dan coba ulang
            currentKeyIndex.set((keyIndex + 1) % activeKeys.size)
            lastResponse = response
        }

        return lastResponse ?: chain.proceed(original)
    }

    companion object {
        private val RETRYABLE_AUTH_OR_QUOTA_CODES = setOf(401, 403, 429)
    }
}
