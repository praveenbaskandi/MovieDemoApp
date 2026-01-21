package com.example.moviedemoapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviedemoapp.data.MovieRepository
import com.example.moviedemoapp.model.Movie
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val nowPlayingMovies: List<Movie> = emptyList(),
    val trendingMovies: List<Movie> = emptyList(),
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val repository = MovieRepository()
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val nowPlayingDeferred = async { repository.getNowPlayingMovies() }
            val trendingDeferred = async { repository.getTrendingMovies() }

            val nowPlayingResult = nowPlayingDeferred.await()
            val trendingResult = trendingDeferred.await()

            if (nowPlayingResult.isSuccess && trendingResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nowPlayingMovies = nowPlayingResult.getOrDefault(emptyList()),
                        trendingMovies = trendingResult.getOrDefault(emptyList())
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load data. Please check your internet connection."
                    )
                }
            }
        }
    }
}
