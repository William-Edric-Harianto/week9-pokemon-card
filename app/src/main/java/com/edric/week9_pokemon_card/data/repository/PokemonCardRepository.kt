package com.edric.week9_pokemon_card.data.repository

import com.edric.week9_pokemon_card.data.remote.PokemonTrackerApi
import com.edric.week9_pokemon_card.model.PokemonCard
import com.edric.week9_pokemon_card.model.PokemonCardPage
import com.edric.week9_pokemon_card.util.safeCall

class PokemonCardRepository(
    private val api: PokemonTrackerApi,
    private val apiKeys: List<String>,
) {
    /**
     * Cache sederhana di memori untuk hasil pencarian agar klik ke halaman detail
     * tidak menghabiskan jatah 100 request/hari.
     */
    private val cardCache = mutableMapOf<String, PokemonCard>()

    /** Cari kartu Pokemon dengan batch 8 kartu dan pagination berbasis offset. */
    suspend fun searchCards(
        query: String,
        offset: Int = 0,
        limit: Int = PAGE_SIZE,
    ): Result<PokemonCardPage> = guarded {
        val page = api.searchCards(
            search = query.trim(),
            limit = limit,
            offset = offset,
        ).toDomain(maxCards = limit, requestedOffset = offset)
        page.cards.forEach { cardCache[it.id] = it }
        page
    }

    /** Ambil detail kartu berdasarkan tcgPlayerId (pakai cache dulu bila sudah ada). */
    suspend fun getCardDetail(cardId: String, forceRefresh: Boolean = false): Result<PokemonCard> {
        if (!forceRefresh) {
            cardCache[cardId]?.let { return Result.success(it) }
        }
        return guarded {
            val dto = api.getCardByTcgPlayerId(tcgPlayerId = cardId).data.firstOrNull()
                ?: throw NoSuchElementException("Card not found")
            val card = dto.toDomain()
            cardCache[card.id] = card
            card
        }
    }

    private suspend fun <T> guarded(block: suspend () -> T): Result<T> {
        if (apiKeys.none { it.isNotBlank() }) {
            return Result.failure(
                IllegalStateException("API key is empty. Add your key(s) to ServiceLocator.API_KEYS.")
            )
        }
        return safeCall(block)
    }

    companion object {
        const val PAGE_SIZE = 8
    }
}
