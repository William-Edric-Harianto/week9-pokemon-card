package com.edric.week9_pokemon_card.data.remote

import com.edric.week9_pokemon_card.data.dto.PokemonCardListResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Daftar endpoint PokemonPriceTracker API v2.
 * Semua fungsi `suspend` agar aman dipanggil dari coroutine.
 * Header Authorization Bearer ditambahkan otomatis oleh ApiKeyInterceptor.
 */
interface PokemonTrackerApi {

    @GET("api/v2/cards")
    suspend fun searchCards(
        @Query("search") search: String,
        @Query("limit") limit: Int = 8,
        @Query("offset") offset: Int = 0,
    ): PokemonCardListResponseDto

    @GET("api/v2/cards")
    suspend fun getCardByTcgPlayerId(
        @Query("tcgPlayerId") tcgPlayerId: String,
    ): PokemonCardListResponseDto
}
