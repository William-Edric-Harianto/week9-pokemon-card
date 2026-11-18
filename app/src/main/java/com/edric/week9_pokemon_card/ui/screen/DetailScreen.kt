package com.edric.week9_pokemon_card.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edric.week9_pokemon_card.model.PokemonCard
import com.edric.week9_pokemon_card.ui.theme.PokeBackground
import com.edric.week9_pokemon_card.ui.theme.PokeCardBorder
import com.edric.week9_pokemon_card.ui.theme.PokeCardSurface
import com.edric.week9_pokemon_card.ui.theme.PokeDetailGradientTop
import com.edric.week9_pokemon_card.ui.theme.PokeGold
import com.edric.week9_pokemon_card.ui.theme.PokeGoldSurface
import com.edric.week9_pokemon_card.ui.theme.PokeGreen
import com.edric.week9_pokemon_card.ui.theme.PokeGreenBorder
import com.edric.week9_pokemon_card.ui.theme.PokeGreenSurface
import com.edric.week9_pokemon_card.ui.theme.PokeRed
import com.edric.week9_pokemon_card.ui.theme.PokeTextPrimary
import com.edric.week9_pokemon_card.ui.theme.PokeTextSecondary
import com.edric.week9_pokemon_card.ui.widgets.ConnectionErrorBox
import com.edric.week9_pokemon_card.ui.widgets.LoadingBox
import com.edric.week9_pokemon_card.ui.widgets.RemoteImage
import com.edric.week9_pokemon_card.ui.widgets.formatUsd
import com.edric.week9_pokemon_card.viewmodel.DetailUiState
import com.edric.week9_pokemon_card.viewmodel.DetailViewModel

@Composable
fun DetailScreen(
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = viewModel(factory = DetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DetailContent(
        state = state,
        onBackClick = onBackClick,
        onRetry = { viewModel.load(forceRefresh = true) },
    )
}

@Composable
fun DetailContent(
    state: DetailUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(containerColor = PokeBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            when (state) {
                DetailUiState.Loading -> LoadingBox(Modifier.align(Alignment.Center))
                is DetailUiState.Error -> ConnectionErrorBox(
                    message = state.message,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
                is DetailUiState.Success -> DetailCardBody(
                    card = state.card,
                    onBackClick = onBackClick,
                )
            }
        }
    }
}

@Composable
private fun DetailCardBody(
    card: PokemonCard,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PokeDetailGradientTop, PokeBackground),
                    ),
                )
                .statusBarsPadding()
                .padding(top = 12.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.25f)),
            )

            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = PokeTextPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }

            RemoteImage(
                url = card.imageHighResUrl ?: card.imageUrl,
                contentDescription = card.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 24.dp)
                    .width(196.dp)
                    .aspectRatio(0.715f)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = card.name,
                color = PokeTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = card.setName,
                color = PokeTextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PriceHighlightCard(
                    label = "Market Price",
                    price = formatUsd(card.marketPrice),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    accentColor = PokeGreen,
                    iconBackground = PokeGreenSurface,
                    borderColor = PokeGreenBorder,
                    modifier = Modifier.weight(1f),
                )
                PriceHighlightCard(
                    label = "Lowest Price",
                    price = formatUsd(card.lowestPrice),
                    icon = Icons.Filled.LocalOffer,
                    accentColor = PokeGold,
                    iconBackground = PokeGoldSurface,
                    borderColor = PokeCardBorder,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))

            SectionHeader(title = "Market Activity")
            Spacer(Modifier.height(10.dp))
            SpecificationCard(
                rows = listOf(
                    "Active Listings" to (card.activeListings?.toString() ?: "-"),
                    "Verified Sellers" to (card.verifiedSellers?.toString() ?: "-"),
                    "Primary Printing" to card.primaryPrinting,
                ),
            )

            Spacer(Modifier.height(22.dp))

            SectionHeader(title = "Card Specifications")
            Spacer(Modifier.height(10.dp))
            SpecificationCard(
                rows = listOf(
                    "Card Number" to card.cardNumber,
                    "Rarity" to card.rarity,
                    "Type" to card.cardType,
                    "Illustrator" to card.illustrator,
                ),
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PokeRed,
                    contentColor = PokeTextPrimary,
                ),
            ) {
                Text(
                    text = "Done",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PriceHighlightCard(
    label: String,
    price: String,
    icon: ImageVector,
    accentColor: Color,
    iconBackground: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = PokeCardSurface,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Text(
                    text = label,
                    color = PokeTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = price,
                color = accentColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = PokeTextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SpecificationCard(rows: List<Pair<String, String>>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = PokeCardSurface,
        border = BorderStroke(1.dp, PokeCardBorder),
    ) {
        Column {
            rows.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        color = PokeTextSecondary,
                        fontSize = 14.sp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = value,
                        color = PokeTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        color = PokeCardBorder.copy(alpha = 0.7f),
                        thickness = 1.dp,
                    )
                }
            }
        }
    }
}
