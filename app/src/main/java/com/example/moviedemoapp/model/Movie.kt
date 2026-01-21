package com.example.moviedemoapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieResponse(
    @SerialName("results")
    val results: List<Movie>,
    @SerialName("page")
    val page: Int,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("total_results")
    val totalResults: Int
)

@Serializable
data class Movie(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("overview")
    val overview: String,
    @SerialName("poster_path")
    val posterPath: String?,
    @SerialName("backdrop_path")
    val backdropPath: String?,
    @SerialName("release_date")
    val releaseDate: String? = null,
    @SerialName("vote_average")
    val voteAverage: Double? = 0.0,
    @SerialName("vote_count")
    val voteCount: Int? = 0
) {
    val fullPosterPath: String
        get() = "https://image.tmdb.org/t/p/w500$posterPath"

    val fullBackdropPath: String
        get() = "https://image.tmdb.org/t/p/original$backdropPath"
}
