package com.example.moviedemoapp.data

import com.example.moviedemoapp.model.Movie
import com.example.moviedemoapp.network.NetworkModule
import java.io.IOException

class MovieRepository {
    private val apiService = NetworkModule.tmdbApiService

    suspend fun getNowPlayingMovies(): Result<List<Movie>> {
        return try {
            val response = apiService.getNowPlayingMovies()
            Result.success(response.results)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrendingMovies(): Result<List<Movie>> {
        return try {
            val response = apiService.getTrendingMovies()
            Result.success(response.results)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieDetails(movieId: Int): Result<Movie> {
        return try {
            val movie = apiService.getMovieDetails(movieId)
            Result.success(movie)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
