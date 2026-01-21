package com.example.moviedemoapp.data

import com.example.moviedemoapp.model.Movie
import com.example.moviedemoapp.network.NetworkModule
import com.example.moviedemoapp.data.local.MovieRealmObject
import com.example.moviedemoapp.data.local.toMovie
import com.example.moviedemoapp.data.local.toRealmObject
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.UpdatePolicy

class MovieRepository {
    private val apiService = NetworkModule.tmdbApiService
    
    private val realm: Realm by lazy {
        val config = RealmConfiguration.Builder(schema = setOf(MovieRealmObject::class))
            .schemaVersion(1)
            .build()
        Realm.open(config)
    }

    suspend fun getNowPlayingMovies(): Result<List<Movie>> {
        return try {
            val response = apiService.getNowPlayingMovies()
            val movies = response.results
            
            realm.write {
                // Clear 'isNowPlaying' flag from all movies first? 
                // Or easier: fetch existing and update.
                // To keep it simple and consistent with previous logic:
                // We'll reset flags for this category.
                
                val currentNowPlaying = query<MovieRealmObject>("isNowPlaying == $0", true).find()
                for (obj in currentNowPlaying) {
                    obj.isNowPlaying = false
                }
                
                for (movie in movies) {
                    // Check if exists to preserve isTrending
                    val existing = query<MovieRealmObject>("id == $0", movie.id).first().find()
                    val isTrending = existing?.isTrending ?: false
                    
                    val realmObj = movie.toRealmObject(isNowPlaying = true, isTrending = isTrending)
                    copyToRealm(realmObj, updatePolicy = UpdatePolicy.ALL)
                }
            }
            
            Result.success(movies)
        } catch (e: Exception) {
            try {
                // Offline fallback
                val cached = realm.query<MovieRealmObject>("isNowPlaying == $0", true).find()
                if (cached.isNotEmpty()) {
                    Result.success(cached.map { it.toMovie() })
                } else {
                    Result.failure(e)
                }
            } catch (realmEx: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getTrendingMovies(): Result<List<Movie>> {
        return try {
            val response = apiService.getTrendingMovies()
            val movies = response.results
            
            realm.write {
                val currentTrending = query<MovieRealmObject>("isTrending == $0", true).find()
                for (obj in currentTrending) {
                    obj.isTrending = false
                }
                
                for (movie in movies) {
                    val existing = query<MovieRealmObject>("id == $0", movie.id).first().find()
                    val isNowPlaying = existing?.isNowPlaying ?: false
                    
                    val realmObj = movie.toRealmObject(isTrending = true, isNowPlaying = isNowPlaying)
                    copyToRealm(realmObj, updatePolicy = UpdatePolicy.ALL)
                }
            }
            
            Result.success(movies)
        } catch (e: Exception) {
             try {
                val cached = realm.query<MovieRealmObject>("isTrending == $0", true).find()
                if (cached.isNotEmpty()) {
                    Result.success(cached.map { it.toMovie() })
                } else {
                     Result.failure(e)
                }
            } catch (realmEx: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getMovieDetails(movieId: Int): Result<Movie> {
        return try {
            val movie = apiService.getMovieDetails(movieId)
            
            realm.write {
                val existing = query<MovieRealmObject>("id == $0", movieId).first().find()
                val isTrending = existing?.isTrending ?: false
                val isNowPlaying = existing?.isNowPlaying ?: false
                
                val realmObj = movie.toRealmObject(isTrending = isTrending, isNowPlaying = isNowPlaying)
                copyToRealm(realmObj, updatePolicy = UpdatePolicy.ALL)
            }
            
            Result.success(movie)
        } catch (e: Exception) {
            try {
                val cached = realm.query<MovieRealmObject>("id == $0", movieId).first().find()
                if (cached != null) {
                    Result.success(cached.toMovie())
                } else {
                    Result.failure(e)
                }
            } catch (realmEx: Exception) {
                Result.failure(e)
            }
        }
    }
}
