package com.edric.week9_pokemon_card

import com.edric.week9_pokemon_card.data.dto.CardPricesDto
import com.edric.week9_pokemon_card.data.dto.MetadataDto
import com.edric.week9_pokemon_card.data.dto.PokemonCardDto
import com.edric.week9_pokemon_card.data.dto.PokemonCardListResponseDto
import com.edric.week9_pokemon_card.data.remote.ApiKeyInterceptor
import com.edric.week9_pokemon_card.data.remote.PokemonTrackerApi
import com.edric.week9_pokemon_card.data.repository.PokemonCardRepository
import com.edric.week9_pokemon_card.data.repository.toDomain
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonCardUnitTest {

    @Test
    fun jsonDeserialization_handlesNumericSetIdAndArrayEnergyType() {
        val rawJson = """
            {
              "data": [
                {
                  "id": 123,
                  "tcgPlayerId": "96392",
                  "setId": 1451,
                  "setName": "XY Promos",
                  "name": "Charizard EX - XY29",
                  "cardNumber": "XY29",
                  "rarity": "Promo",
                  "hp": 180,
                  "pokemonType": "Fire",
                  "energyType": ["Fire"],
                  "flavorText": null,
                  "artist": "Eske Yoshinob",
                  "imageCdnUrl400": "https://example.com/xy29.jpg",
                  "prices": {
                    "market": 21.78,
                    "low": 2.84,
                    "listings": 109,
                    "sellers": 88,
                    "primaryPrinting": "Holofoil"
                  }
                }
              ],
              "metadata": {
                "total": 241,
                "count": 1,
                "limit": 8,
                "offset": 0,
                "hasMore": false
              }
            }
        """.trimIndent()

        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val dto = json.decodeFromString<PokemonCardListResponseDto>(rawJson)
        val page = dto.toDomain(maxCards = 8, requestedOffset = 0)

        assertEquals(1, page.cards.size)
        assertEquals(241, page.total)
        assertFalse(page.hasMore)
        val card = page.cards.first()
        assertEquals("96392", card.id)
        assertEquals("Charizard EX - XY29", card.name)
        assertEquals("XY Promos", card.setName)
        assertEquals("XY29", card.cardNumber)
        assertEquals("180", card.hp)
        assertEquals("Fire", card.cardType)
        assertEquals("Eske Yoshinob", card.illustrator)
        assertEquals(21.78, card.marketPrice ?: 0.0, 0.001)
        assertEquals(2.84, card.lowestPrice ?: 0.0, 0.001)
        assertEquals(109, card.activeListings)
        assertEquals(88, card.verifiedSellers)
    }

    @Test
    fun mapper_capsPageAtEightCards_andPreservesTotal() {
        val dtos = (1..20).map { index ->
            PokemonCardDto(
                tcgPlayerId = JsonPrimitive("id_$index"),
                name = "Charizard EX - XY$index",
                setName = "XY Promos",
                cardNumber = JsonPrimitive("XY$index"),
                rarity = "Promo",
                hp = JsonPrimitive(180),
                cardType = JsonPrimitive("Fire"),
                illustrator = "Eske Yoshinob",
                imageCdnUrl400 = "https://example.com/$index.jpg",
                prices = CardPricesDto(
                    market = JsonPrimitive(21.78),
                    low = JsonPrimitive(2.84),
                    listings = JsonPrimitive(109),
                    sellers = JsonPrimitive(88),
                    primaryPrinting = "Holofoil",
                ),
            )
        }
        val response = PokemonCardListResponseDto(
            data = dtos,
            metadata = MetadataDto(total = 241, count = 20, limit = 20, offset = 0, hasMore = true),
        )

        val page = response.toDomain(maxCards = 8, requestedOffset = 0)
        assertEquals(8, page.cards.size)
        assertEquals(241, page.total)
        assertTrue(page.hasMore)
        assertEquals("id_1", page.cards.first().id)
        assertEquals("180", page.cards.first().hp)
        assertEquals("Fire", page.cards.first().cardType)
        assertEquals("Eske Yoshinob", page.cards.first().illustrator)
        assertEquals(21.78, page.cards.first().marketPrice ?: 0.0, 0.001)
    }

    @Test
    fun repository_loadsEightCardBatchesWithLazyOffset_andCachesDetail() = runBlocking {
        val requestedOffsets = mutableListOf<Int>()
        val requestedLimits = mutableListOf<Int>()
        var detailCalls = 0

        val fakeApi = object : PokemonTrackerApi {
            override suspend fun searchCards(search: String, limit: Int, offset: Int): PokemonCardListResponseDto {
                requestedLimits.add(limit)
                requestedOffsets.add(offset)
                val slice = (offset + 1..minOf(offset + limit, 14)).map { i ->
                    PokemonCardDto(tcgPlayerId = JsonPrimitive("card_$i"), name = "$search $i")
                }
                return PokemonCardListResponseDto(
                    data = slice,
                    metadata = MetadataDto(
                        total = 14,
                        count = slice.size,
                        limit = limit,
                        offset = offset,
                        hasMore = offset + slice.size < 14,
                    ),
                )
            }

            override suspend fun getCardByTcgPlayerId(tcgPlayerId: String): PokemonCardListResponseDto {
                detailCalls++
                return PokemonCardListResponseDto(
                    data = listOf(PokemonCardDto(tcgPlayerId = JsonPrimitive(tcgPlayerId), name = "Fetched $tcgPlayerId")),
                )
            }
        }

        val repository = PokemonCardRepository(fakeApi, listOf("key_1", "key_2"))
        val firstBatch = repository.searchCards("Charizard", offset = 0).getOrThrow()
        assertEquals(8, firstBatch.cards.size)
        assertEquals(14, firstBatch.total)
        assertTrue(firstBatch.hasMore)

        val secondBatch = repository.searchCards("Charizard", offset = 8).getOrThrow()
        assertEquals(6, secondBatch.cards.size)
        assertFalse(secondBatch.hasMore)

        assertEquals(listOf(8, 8), requestedLimits)
        assertEquals(listOf(0, 8), requestedOffsets)

        val detailResult = repository.getCardDetail("card_1")
        assertTrue(detailResult.isSuccess)
        assertEquals("Charizard 1", detailResult.getOrThrow().name)
        assertEquals(0, detailCalls)
    }

    @Test
    fun repository_failsWhenApiKeyListIsEmpty() = runBlocking {
        val fakeApi = object : PokemonTrackerApi {
            override suspend fun searchCards(search: String, limit: Int, offset: Int) = PokemonCardListResponseDto()
            override suspend fun getCardByTcgPlayerId(tcgPlayerId: String) = PokemonCardListResponseDto()
        }
        val repository = PokemonCardRepository(fakeApi, emptyList())
        val result = repository.searchCards("Charizard")
        assertTrue(result.isFailure)
    }

    @Test
    fun interceptor_rotatesThroughMultipleApiKeys_onRateLimit() {
        val interceptor = ApiKeyInterceptor(listOf("key_first", "key_second"))
        val seenHeaders = mutableListOf<String?>()

        val chain = FakeChain { request ->
            val auth = request.header("Authorization")
            seenHeaders.add(auth)
            val code = if (auth == "Bearer key_first") 429 else 200
            Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(if (code == 429) "Too Many Requests" else "OK")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()
        }

        val response = interceptor.intercept(chain)
        assertEquals(200, response.code)
        assertEquals(listOf("Bearer key_first", "Bearer key_second"), seenHeaders)
    }

    private class FakeChain(
        private val handler: (Request) -> Response,
    ) : Interceptor.Chain {
        private val initialRequest = Request.Builder()
            .url("https://www.pokemonpricetracker.com/api/v2/cards?search=Charizard&limit=8&offset=0")
            .build()

        override fun request(): Request = initialRequest
        override fun proceed(request: Request): Response = handler(request)
        override fun connection(): Connection? = null
        override fun call(): Call = throw UnsupportedOperationException()
        override fun connectTimeoutMillis(): Int = 10_000
        override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
        override fun readTimeoutMillis(): Int = 10_000
        override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
        override fun writeTimeoutMillis(): Int = 10_000
        override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
    }
}
