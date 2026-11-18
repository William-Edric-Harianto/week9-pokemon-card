package com.edric.week9_pokemon_card.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.edric.week9_pokemon_card.data.remote.ServiceLocator
import com.edric.week9_pokemon_card.data.repository.PokemonCardRepository
import com.edric.week9_pokemon_card.model.PokemonCard
import com.edric.week9_pokemon_card.navigation.DetailRoute
import com.edric.week9_pokemon_card.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val card: PokemonCard) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: PokemonCardRepository,
) : ViewModel() {

    private val cardId: String = savedStateHandle.toRoute<DetailRoute>().cardId

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load(forceRefresh: Boolean = false) {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            repository.getCardDetail(cardId = cardId, forceRefresh = forceRefresh)
                .onSuccess { _uiState.value = DetailUiState.Success(it) }
                .onFailure { _uiState.value = DetailUiState.Error(it.toUserMessage()) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                DetailViewModel(createSavedStateHandle(), ServiceLocator.pokemonCardRepository)
            }
        }
    }
}
