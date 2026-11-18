package com.edric.week9_pokemon_card.data.repository

import com.edric.week9_pokemon_card.data.dto.PokemonCardDto
import com.edric.week9_pokemon_card.data.dto.PokemonCardListResponseDto
import com.edric.week9_pokemon_card.model.PokemonCard
import com.edric.week9_pokemon_card.model.PokemonCardPage
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

private fun JsonPrimitive?.asCleanString(): String? =
    this?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() && it != "null" }

private fun JsonPrimitive?.asDouble(): Double? =
    this?.doubleOrNull ?: this?.contentOrNull?.toDoubleOrNull()

private fun JsonPrimitive?.asInt(): Int? =
    this?.intOrNull ?: this?.doubleOrNull?.toInt() ?: this?.contentOrNull?.toIntOrNull()

private fun JsonElement?.firstTextValue(): String? = when (this) {
    is JsonPrimitive -> asCleanString()
    is JsonArray -> firstNotNullOfOrNull { (it as? JsonPrimitive)?.asCleanString() }
    else -> null
}

fun PokemonCardDto.toDomain(): PokemonCard {
    val tcgIdStr = tcgPlayerId.asCleanString()
    val idStr = id.asCleanString()
    val cardNumberStr = cardNumber.asCleanString()

    val resolvedId = tcgIdStr
        ?: idStr
        ?: "${name}_${cardNumberStr.orEmpty()}"

    val resolvedType = cardType.firstTextValue()
        ?: pokemonType.firstTextValue()
        ?: energyType.firstTextValue()
        ?: type.firstTextValue()
        ?: types.firstTextValue()
        ?: supertype?.takeIf { it.isNotBlank() }
        ?: "-"

    val resolvedIllustrator = illustrator?.takeIf { it.isNotBlank() }
        ?: artist?.takeIf { it.isNotBlank() }
        ?: "-"

    val gridImage = imageCdnUrl400
        ?: imageCdnUrl
        ?: imageCdnUrl800
        ?: imageCdnUrl200
        ?: imageUrl

    val detailImage = imageCdnUrl800
        ?: imageCdnUrl400
        ?: imageCdnUrl
        ?: imageUrl
        ?: imageCdnUrl200

    return PokemonCard(
        id = resolvedId,
        name = name.ifBlank { "Unknown Card" },
        setName = setName?.takeIf { it.isNotBlank() } ?: "Unknown Set",
        cardNumber = cardNumberStr ?: "-",
        rarity = rarity?.takeIf { it.isNotBlank() } ?: "Common",
        hp = hp.asCleanString(),
        imageUrl = gridImage,
        imageHighResUrl = detailImage,
        marketPrice = prices?.market.asDouble(),
        lowestPrice = prices?.low.asDouble() ?: prices?.lowPrice.asDouble(),
        activeListings = prices?.listings.asInt() ?: prices?.totalListings.asInt(),
        verifiedSellers = prices?.sellers.asInt(),
        primaryPrinting = prices?.primaryPrinting?.takeIf { it.isNotBlank() } ?: "Normal",
        cardType = resolvedType,
        illustrator = resolvedIllustrator,
    )
}

fun PokemonCardListResponseDto.toDomain(
    maxCards: Int = PokemonCardRepository.PAGE_SIZE,
    requestedOffset: Int = 0,
): PokemonCardPage {
    val mappedCards = data.take(maxCards).map { it.toDomain() }
    val currentOffset = metadata?.offset ?: requestedOffset
    val total = metadata?.total ?: (currentOffset + mappedCards.size)
    val hasMore = metadata?.hasMore ?: (
        mappedCards.size >= maxCards && (metadata?.total == null || currentOffset + mappedCards.size < metadata.total)
    )
    return PokemonCardPage(
        cards = mappedCards,
        total = total,
        offset = currentOffset,
        hasMore = hasMore,
    )
}
