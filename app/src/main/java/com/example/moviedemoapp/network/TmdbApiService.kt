package com.example.moviedemoapp.network

import com.example.moviedemoapp.model.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {

    @GET("movie/popular")
    suspend fun getNowPlayingMovies(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("language") language: String = "en-US"
    ): MovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @retrofit2.http.Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): com.example.moviedemoapp.model.Movie
}
