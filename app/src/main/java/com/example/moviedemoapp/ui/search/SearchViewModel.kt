package com.example.moviedemoapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviedemoapp.data.MovieRepository
import com.example.moviedemoapp.model.Movie
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SearchViewModel : ViewModel() {
    private val repository = MovieRepository()
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        
        if (newQuery.isBlank()) {
             _uiState.update { it.copy(results = emptyList(), error = null, isLoading = false) }
             return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            performSearch(newQuery)
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val result = repository.searchMovies(query)
        
        if (result.isSuccess) {
            _uiState.update { it.copy(isLoading = false, results = result.getOrDefault(emptyList())) }
        } else {
             _uiState.update { it.copy(isLoading = false, error = "Search failed. Please try again.") }
        }
    }
}
