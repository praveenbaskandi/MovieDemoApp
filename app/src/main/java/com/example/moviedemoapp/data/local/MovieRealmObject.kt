package com.example.moviedemoapp.data.local

import com.example.moviedemoapp.model.Movie
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

open class MovieRealmObject : RealmObject {
    @PrimaryKey
    var id: Int = 0
    var title: String = ""
    var overview: String = ""
    var posterPath: String? = null
    var backdropPath: String? = null
    var releaseDate: String? = null
    var voteAverage: Double? = null
    var voteCount: Int? = null
    var isTrending: Boolean = false
    var isNowPlaying: Boolean = false
    var cachedAt: Long = System.currentTimeMillis()
}

fun MovieRealmObject.toMovie(): Movie {
    return Movie(
        id = id,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        voteCount = voteCount
    )
}

fun Movie.toRealmObject(isTrending: Boolean = false, isNowPlaying: Boolean = false): MovieRealmObject {
    return MovieRealmObject().apply {
        this.id = this@toRealmObject.id
        this.title = this@toRealmObject.title
        this.overview = this@toRealmObject.overview
        this.posterPath = this@toRealmObject.posterPath
        this.backdropPath = this@toRealmObject.backdropPath
        this.releaseDate = this@toRealmObject.releaseDate
        this.voteAverage = this@toRealmObject.voteAverage
        this.voteCount = this@toRealmObject.voteCount
        this.isTrending = isTrending
        this.isNowPlaying = isNowPlaying
    }
}
