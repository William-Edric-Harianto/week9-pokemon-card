package com.edric.week9_pokemon_card.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edric.week9_pokemon_card.model.PokemonFilters
import com.edric.week9_pokemon_card.ui.theme.PokeBackground
import com.edric.week9_pokemon_card.ui.theme.PokeCardBorder
import com.edric.week9_pokemon_card.ui.theme.PokeCardSurface
import com.edric.week9_pokemon_card.ui.theme.PokeChipSelectedSurface
import com.edric.week9_pokemon_card.ui.theme.PokeChipSurface
import com.edric.week9_pokemon_card.ui.theme.PokeHeaderSurface
import com.edric.week9_pokemon_card.ui.theme.PokeRed
import com.edric.week9_pokemon_card.ui.theme.PokeTextPrimary
import com.edric.week9_pokemon_card.ui.theme.PokeTextSecondary
import com.edric.week9_pokemon_card.ui.widgets.ConnectionErrorBox
import com.edric.week9_pokemon_card.ui.widgets.EmptyResultsBox
import com.edric.week9_pokemon_card.ui.widgets.LiveApiBadge
import com.edric.week9_pokemon_card.ui.widgets.LoadingBox
import com.edric.week9_pokemon_card.ui.widgets.PokeballBadge
import com.edric.week9_pokemon_card.ui.widgets.PokemonCardGridItem
import com.edric.week9_pokemon_card.viewmodel.DashboardUiState
import com.edric.week9_pokemon_card.viewmodel.DashboardViewModel

/** STATEFUL: terhubung ke ViewModel, mengumpulkan state, dan meneruskan event. */
@Composable
fun DashboardScreen(
    onCardClick: (String) -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        state = state,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearSearch = viewModel::onClearSearch,
        onSearchSubmit = viewModel::onSearchSubmit,
        onFilterChipClick = viewModel::onFilterChipClick,
        onCardClick = onCardClick,
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadNextWindow,
        onRetryLoadMore = viewModel::retryLoadMore,
    )
}

/** STATELESS: hanya menggambar UI dari state -> mudah di-preview dan dites. */
@Composable
fun DashboardContent(
    state: DashboardUiState,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSearchSubmit: () -> Unit,
    onFilterChipClick: (String) -> Unit,
    onCardClick: (String) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val gridState = rememberLazyGridState()

    // Lazy offset: load 8 cards per request, prefetching when user reaches the 6th card (2 cards before end)
    val nearEnd by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val hasScrolled = gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0
            info.totalItemsCount > 0 && hasScrolled && lastVisible >= info.totalItemsCount - 4
        }
    }
    LaunchedEffect(nearEnd) {
        if (nearEnd) onLoadMore()
    }

    LaunchedEffect(state.activeQuery) {
        gridState.scrollToItem(0)
    }

    Scaffold(
        containerColor = PokeBackground,
        topBar = { PokeTrackerTopBar() },
    ) { innerPadding ->
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "search-bar", span = { GridItemSpan(2) }) {
                PokemonSearchBar(
                    query = state.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onClear = onClearSearch,
                    onSubmit = {
                        focusManager.clearFocus()
                        onSearchSubmit()
                    },
                )
            }

            item(key = "quick-filters", span = { GridItemSpan(2) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(PokemonFilters.quickSearches, key = { it }) { pokemonName ->
                        val isSelected = state.activeQuery.equals(pokemonName, ignoreCase = true)
                        QuickFilterChip(
                            label = pokemonName,
                            selected = isSelected,
                            onClick = {
                                focusManager.clearFocus()
                                onFilterChipClick(pokemonName)
                            },
                        )
                    }
                }
            }

            when {
                state.isLoadingInitial -> {
                    item(key = "loading", span = { GridItemSpan(2) }) {
                        LoadingBox(modifier = Modifier.padding(top = 64.dp))
                    }
                }

                state.initialError != null -> {
                    item(key = "error", span = { GridItemSpan(2) }) {
                        ConnectionErrorBox(
                            message = state.initialError,
                            onRetry = onRetry,
                            modifier = Modifier.padding(top = 56.dp),
                        )
                    }
                }

                state.cards.isEmpty() -> {
                    item(key = "empty", span = { GridItemSpan(2) }) {
                        EmptyResultsBox(
                            query = state.activeQuery,
                            modifier = Modifier.padding(top = 48.dp),
                        )
                    }
                }

                else -> {
                    item(key = "results-header", span = { GridItemSpan(2) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val displayTotal = if (state.totalCount > 0) state.totalCount else state.cards.size
                            Text(
                                text = "Market Results ($displayTotal)",
                                color = PokeTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "USD • Near Mint",
                                color = PokeTextSecondary,
                                fontSize = 12.sp,
                            )
                        }
                    }

                    items(state.cards, key = { it.id }) { card ->
                        PokemonCardGridItem(
                            card = card,
                            onClick = { onCardClick(card.id) },
                        )
                    }

                    item(key = "grid-footer", span = { GridItemSpan(2) }) {
                        when {
                            state.loadMoreError != null -> {
                                ConnectionErrorBox(
                                    message = state.loadMoreError,
                                    onRetry = onRetryLoadMore,
                                )
                            }

                            state.isLoadingMore -> {
                                LoadingBox()
                            }

                            state.endReached -> {
                                Text(
                                    text = "No more cards",
                                    color = PokeTextSecondary,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                )
                            }

                            !gridState.canScrollForward -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    OutlinedButton(
                                        onClick = onLoadMore,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, PokeCardBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = PokeTextPrimary,
                                        ),
                                    ) {
                                        Text(
                                            text = "Load more",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PokeTrackerTopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PokeHeaderSurface)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PokeballBadge()
                Column {
                    Text(
                        text = "PokéTracker",
                        color = PokeTextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Real-time TCG Card Valuation",
                        color = PokeTextSecondary,
                        fontSize = 12.sp,
                    )
                }
            }

            LiveApiBadge()
        }
        HorizontalDivider(color = PokeCardBorder, thickness = 1.dp)
    }
}

@Composable
private fun PokemonSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        color = PokeCardSurface,
        border = BorderStroke(1.dp, PokeCardBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = PokeRed,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onSubmit),
            )
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search Pokemon cards...",
                        color = PokeTextSecondary,
                        fontSize = 15.sp,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = PokeTextPrimary,
                        fontSize = 15.sp,
                    ),
                    cursorBrush = SolidColor(PokeRed),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = PokeTextSecondary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) PokeChipSelectedSurface else PokeChipSurface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) PokeTextSecondary.copy(alpha = 0.55f) else PokeCardBorder,
        ),
    ) {
        Text(
            text = label,
            color = if (selected) PokeTextPrimary else PokeTextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}
