package com.edric.week9_pokemon_card.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.edric.week9_pokemon_card.data.remote.ServiceLocator
import com.edric.week9_pokemon_card.data.repository.PokemonCardRepository
import com.edric.week9_pokemon_card.model.PokemonCard
import com.edric.week9_pokemon_card.util.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Seluruh kondisi layar Dashboard dalam satu objek, mendukung lazy offset pagination (window 6 kartu). */
data class DashboardUiState(
    val searchQuery: String = "Charizard",
    val activeQuery: String = "Charizard",
    val cards: List<PokemonCard> = emptyList(),
    val totalCount: Int = 0,
    val isLoadingInitial: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val initialError: String? = null,
    val loadMoreError: String? = null,
)

class DashboardViewModel(
    private val repository: PokemonCardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var nextOffset = 0

    init {
        startNewSearch("Charizard")
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onClearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
    }

    fun onSearchSubmit() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return
        startNewSearch(query)
    }

    fun onFilterChipClick(pokemonName: String) {
        _uiState.update { it.copy(searchQuery = pokemonName) }
        startNewSearch(pokemonName)
    }

    fun retry() {
        val query = _uiState.value.searchQuery.trim().ifEmpty { _uiState.value.activeQuery }
        startNewSearch(query)
    }

    /** Dipanggil oleh LazyVerticalGrid saat user scroll mendekati akhir (lazy offset, 8 kartu per batch). */
    fun loadNextWindow() {
        val s = _uiState.value
        if (s.isLoadingInitial || s.isLoadingMore || s.endReached || s.initialError != null || s.loadMoreError != null) {
            return
        }

        _uiState.update { it.copy(isLoadingMore = true) }
        searchJob = viewModelScope.launch {
            repository.searchCards(
                query = s.activeQuery,
                offset = nextOffset,
                limit = PokemonCardRepository.PAGE_SIZE,
            )
                .onSuccess { page ->
                    nextOffset = page.offset + page.cards.size
                    _uiState.update {
                        it.copy(
                            cards = (it.cards + page.cards).distinctBy { card -> card.id },
                            totalCount = page.total,
                            isLoadingMore = false,
                            endReached = !page.hasMore || page.cards.isEmpty(),
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            loadMoreError = e.toUserMessage(),
                        )
                    }
                }
        }
    }

    fun retryLoadMore() {
        _uiState.update { it.copy(loadMoreError = null) }
        loadNextWindow()
    }

    private fun startNewSearch(query: String) {
        searchJob?.cancel()
        nextOffset = 0
        _uiState.update {
            it.copy(
                activeQuery = query,
                cards = emptyList(),
                totalCount = 0,
                isLoadingInitial = true,
                isLoadingMore = false,
                endReached = false,
                initialError = null,
                loadMoreError = null,
            )
        }
        searchJob = viewModelScope.launch {
            repository.searchCards(
                query = query,
                offset = 0,
                limit = PokemonCardRepository.PAGE_SIZE,
            )
                .onSuccess { page ->
                    nextOffset = page.offset + page.cards.size
                    _uiState.update {
                        it.copy(
                            cards = page.cards.distinctBy { card -> card.id },
                            totalCount = page.total,
                            isLoadingInitial = false,
                            endReached = !page.hasMore || page.cards.isEmpty(),
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoadingInitial = false,
                            initialError = e.toUserMessage(),
                        )
                    }
                }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { DashboardViewModel(ServiceLocator.pokemonCardRepository) }
        }
    }
}
