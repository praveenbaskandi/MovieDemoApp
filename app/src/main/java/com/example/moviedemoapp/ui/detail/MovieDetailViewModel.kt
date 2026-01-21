package com.example.moviedemoapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviedemoapp.data.MovieRepository
import com.example.moviedemoapp.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieDetailUiState(
    val isLoading: Boolean = false,
    val movie: Movie? = null,
    val error: String? = null
)

class MovieDetailViewModel : ViewModel() {
    private val repository = MovieRepository()
    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    fun loadMovie(movieId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getMovieDetails(movieId)
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(isLoading = false, movie = result.getOrNull())
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load movie details.")
                }
            }
        }
    }
}
