package com.edric.week9_pokemon_card.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * DTO = struktur JSON persis dari PokemonPriceTracker API v2.
 * Field yang tipe JSON-nya bervariasi (angka/string atau string/array) memakai
 * JsonPrimitive/JsonElement agar deserialisasi tidak gagal saat server mengirim variasi tipe.
 */
@Serializable
data class PokemonCardListResponseDto(
    val data: List<PokemonCardDto> = emptyList(),
    val metadata: MetadataDto? = null,
)

@Serializable
data class MetadataDto(
    val total: Int? = null,
    val count: Int? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val hasMore: Boolean? = null,
)

@Serializable
data class PokemonCardDto(
    val id: JsonPrimitive? = null,
    val tcgPlayerId: JsonPrimitive? = null,
    val name: String = "",
    val setName: String? = null,
    val setId: JsonPrimitive? = null,
    val cardNumber: JsonPrimitive? = null,
    val rarity: String? = null,
    val hp: JsonPrimitive? = null,
    val cardType: JsonElement? = null,
    val pokemonType: JsonElement? = null,
    val energyType: JsonElement? = null,
    val type: JsonElement? = null,
    val types: JsonElement? = null,
    val supertype: String? = null,
    val artist: String? = null,
    val illustrator: String? = null,
    val imageCdnUrl800: String? = null,
    val imageCdnUrl400: String? = null,
    val imageCdnUrl200: String? = null,
    val imageCdnUrl: String? = null,
    val imageUrl: String? = null,
    val prices: CardPricesDto? = null,
)

@Serializable
data class CardPricesDto(
    val market: JsonPrimitive? = null,
    val low: JsonPrimitive? = null,
    val lowPrice: JsonPrimitive? = null,
    val mid: JsonPrimitive? = null,
    val high: JsonPrimitive? = null,
    val sellers: JsonPrimitive? = null,
    val listings: JsonPrimitive? = null,
    val totalListings: JsonPrimitive? = null,
    val primaryPrinting: String? = null,
    val lastUpdated: String? = null,
)
