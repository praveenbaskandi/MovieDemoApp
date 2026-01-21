package com.example.moviedemoapp.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository()
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Collect flows from repository (Single Source of Truth)
        viewModelScope.launch {
            repository.getNowPlayingMoviesStream().collect { movies ->
                _uiState.update { it.copy(nowPlayingMovies = movies) }
            }
        }
        
        viewModelScope.launch {
            repository.getTrendingMoviesStream().collect { movies ->
                _uiState.update { it.copy(trendingMovies = movies) }
            }
        }
        
        refreshData()
    }

    private fun refreshData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val nowPlayingDeferred = async { repository.refreshNowPlayingMovies() }
            val trendingDeferred = async { repository.refreshTrendingMovies() }
            
            val nowPlayingResult = nowPlayingDeferred.await()
            val trendingResult = trendingDeferred.await()
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = if (nowPlayingResult.isFailure || trendingResult.isFailure) "Failed to refresh data" else null
                ) 
            }
        }
    }
}
