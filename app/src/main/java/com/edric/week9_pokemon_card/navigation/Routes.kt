package com.edric.week9_pokemon_card.navigation

import kotlinx.serialization.Serializable

@Serializable
data object DashboardRoute

@Serializable
data class DetailRoute(val cardId: String)
