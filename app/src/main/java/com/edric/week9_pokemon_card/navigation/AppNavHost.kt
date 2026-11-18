package com.edric.week9_pokemon_card.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edric.week9_pokemon_card.ui.screen.DashboardScreen
import com.edric.week9_pokemon_card.ui.screen.DetailScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = DashboardRoute) {
        composable<DashboardRoute> {
            DashboardScreen(
                onCardClick = { cardId -> navController.navigate(DetailRoute(cardId)) },
            )
        }
        composable<DetailRoute> {
            DetailScreen(
                onBackClick = { navController.navigateUp() },
            )
        }
    }
}
