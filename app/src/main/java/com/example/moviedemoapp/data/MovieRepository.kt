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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MovieRepository {
    private val apiService = NetworkModule.tmdbApiService
    
    private val realm: Realm by lazy {
        val config = RealmConfiguration.Builder(schema = setOf(MovieRealmObject::class))
            .schemaVersion(1)
            .build()
        Realm.open(config)
    }

    fun getNowPlayingMoviesStream(): Flow<List<Movie>> {
        return realm.query<MovieRealmObject>("isNowPlaying == $0", true)
            .asFlow()
            .map { resultChange ->
                resultChange.list.map { it.toMovie() }
            }
    }

    suspend fun refreshNowPlayingMovies(): Result<Unit> {
        return try {
            val response = apiService.getNowPlayingMovies()
            val movies = response.results
            
            realm.write {
                val currentNowPlaying = query<MovieRealmObject>("isNowPlaying == $0", true).find()
                for (obj in currentNowPlaying) {
                    obj.isNowPlaying = false
                }
                
                for (movie in movies) {
                    val existing = query<MovieRealmObject>("id == $0", movie.id).first().find()
                    val isTrending = existing?.isTrending ?: false
                    
                    val realmObj = movie.toRealmObject(isNowPlaying = true, isTrending = isTrending)
                    copyToRealm(realmObj, updatePolicy = UpdatePolicy.ALL)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTrendingMoviesStream(): Flow<List<Movie>> {
        return realm.query<MovieRealmObject>("isTrending == $0", true)
            .asFlow()
            .map { resultChange ->
                resultChange.list.map { it.toMovie() }
            }
    }

    suspend fun refreshTrendingMovies(): Result<Unit> {
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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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

    suspend fun searchMovies(query: String): Result<List<Movie>> {
        return try {
            val response = apiService.searchMovies(query)
            val movies = response.results
            
            // Cache search results in Realm so we can view their details?
            // If we don't save them, 'getMovieDetails' logic might fail if it relies on 'id' existence 
            // OR 'getMovieDetails' actually fetches from network so it's fine.
            // BUT for offline continuity, maybe save them.
            // No, 'getMovieDetails' logic is: Try Network -> Upsert -> Return.
            // So if we click a search result, it will fetch details and save.
            // IF we are offline when searching... search won't work anyway.
            // IF we search, go offline, then click... detail fetch will fail if not cached.
            // So beneficial to cache search results.
            // HOWEVER, we don't have a 'isSearchResult' flag, so they might clutter the database 
            // without belonging to Trending or NowPlaying.
            // As long as we don't query them in those lists, it's fine.
            
             realm.write {
                for (movie in movies) {
                    val existing = query<MovieRealmObject>("id == $0", movie.id).first().find()
                    val isTrending = existing?.isTrending ?: false
                    val isNowPlaying = existing?.isNowPlaying ?: false
                    
                    val realmObj = movie.toRealmObject(isTrending = isTrending, isNowPlaying = isNowPlaying)
                    copyToRealm(realmObj, updatePolicy = UpdatePolicy.ALL)
                }
            }
            
            Result.success(movies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
