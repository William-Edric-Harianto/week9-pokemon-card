package com.edric.week9_pokemon_card.model

/** Model domain untuk kartu Pokemon yang dipakai di UI (Dashboard & Detail). */
data class PokemonCard(
    val id: String,
    val name: String,
    val setName: String,
    val cardNumber: String,
    val rarity: String,
    val hp: String?,
    val imageUrl: String?,
    val imageHighResUrl: String?,
    val marketPrice: Double?,
    val lowestPrice: Double?,
    val activeListings: Int?,
    val verifiedSellers: Int?,
    val primaryPrinting: String,
    val cardType: String,
    val illustrator: String,
)

/** Satu jendela (6 kartu) hasil pencarian dari API dengan dukungan lazy offset. */
data class PokemonCardPage(
    val cards: List<PokemonCard>,
    val total: Int,
    val offset: Int,
    val hasMore: Boolean,
)

/** Pilihan filter cepat yang ditampilkan sebagai horizontal chips di Dashboard. */
object PokemonFilters {
    val quickSearches = listOf(
        "Charizard",
        "Pikachu",
        "Mewtwo",
        "Gengar",
        "Rayquaza",
        "Umbreon",
    )
}
