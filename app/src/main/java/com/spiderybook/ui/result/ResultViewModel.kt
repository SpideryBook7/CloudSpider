package com.spiderybook.ui.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.spiderybook.data.repository.LoadRepository
import com.spiderybook.domain.model.LoadResponse
import com.spiderybook.ui.common.BaseViewModel
import com.spiderybook.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val loadRepository: LoadRepository,
    private val localRepository: com.spiderybook.data.repository.LocalRepository,
    private val tmdbRepository: com.spiderybook.data.repository.TMDBRepository
) : BaseViewModel() {

    private val _result = MutableLiveData<Resource<LoadResponse>>()
    val result: LiveData<Resource<LoadResponse>> = _result

    fun load(apiName: String, url: String) = launchIO {
        _result.setLoading()
        val data = loadRepository.load(apiName, url)
        if (data != null) {
            _result.setSuccess(data)
            
            // TMDB Metadata loading for rich UI covers and ratings (Enabled for all providers)
            launchIO {
                val meta = tmdbRepository.getMetadata(data.name)
                if (meta != null) {
                    android.util.Log.d("TMDB_DEBUG", "Found metadata for id: ${meta.id}, type: ${meta.mediaType}")
                    _tmdbMetadata.postValue(meta)
                    // If we found a valid media item, fetch its trailer
                    if (meta.id != null && meta.mediaType != null) {
                        val videoKey = tmdbRepository.getTrailerKey(meta.id, meta.mediaType)
                        android.util.Log.d("TMDB_DEBUG", "Fetched Trailer Key: $videoKey for id: ${meta.id}")
                        if (videoKey != null) {
                            _youtubeTrailerKey.postValue(videoKey)
                        }
                    } else {
                        android.util.Log.d("TMDB_DEBUG", "MediaType or ID is null. Cannot fetch trailer.")
                    }
                } else {
                    android.util.Log.d("TMDB_DEBUG", "No TMDB metadata found for ${data.name}")
                }
            }
        } else {
            _result.setError("Failed to load details")
        }
    }

    private val _tmdbMetadata = MutableLiveData<com.spiderybook.data.remote.dto.TMDBMediaItem?>()
    val tmdbMetadata: LiveData<com.spiderybook.data.remote.dto.TMDBMediaItem?> = _tmdbMetadata

    private val _youtubeTrailerKey = MutableLiveData<String>()
    val youtubeTrailerKey: LiveData<String> = _youtubeTrailerKey

    // Download Status
    private val _downloadStatus = MutableLiveData<Resource<String>>()
    val downloadStatus: LiveData<Resource<String>> = _downloadStatus

    fun downloadEpisode(
        apiName: String, 
        episodeUrl: String, 
        fileName: String, 
        downloadManager: com.spiderybook.data.manager.AppDownloadManager
    ) = launchIO {
        _downloadStatus.postValue(Resource.Loading)
        val links = mutableListOf<com.spiderybook.plugins.MainAPI.ExtractorLink>()
        
        val success = loadRepository.loadLinks(apiName, episodeUrl) { link ->
            links.add(link)
        }
        
        if (success && links.isNotEmpty()) {
            val validMediaLinks = links.filter { !it.name.contains("Web/Raw") }
            
            val bestLink = validMediaLinks.firstOrNull { it.url.contains(".mp4") && !it.isM3u8 } 
                ?: validMediaLinks.firstOrNull { !it.isM3u8 } 
                ?: validMediaLinks.firstOrNull()

            when {
                bestLink == null -> {
                    _downloadStatus.postValue(Resource.Error("No se encontraron enlaces de video extraíbles para $fileName"))
                }
                bestLink.isM3u8 -> {
                    _downloadStatus.postValue(Resource.Error("Servidor con formato cerrado (M3U8). ¡Selecciona opciones como Streamtape o reproductores directos!"))
                }
                else -> {
                    downloadManager.download(bestLink.url, fileName, bestLink.referer)
                    _downloadStatus.postValue(Resource.Success("Iniciando descarga: $fileName en Alta Calidad"))
                }
            }
        } else {
            _downloadStatus.postValue(Resource.Error("Error al obtener el enlace de descarga para $fileName"))
        }
    }

    // Favorites & History Logic
    fun isFavorite(url: String, name: String) = localRepository.isFavorite(url, name).asLiveData()
    val history = localRepository.getHistory().asLiveData()

    fun toggleFavorite(currentItem: LoadResponse?) = launchIO {
        if (currentItem == null) return@launchIO
    }
    
    fun saveEpisodeProgress(data: LoadResponse, episode: com.spiderybook.domain.model.Episode) = launchIO {
        localRepository.insertHistory(
            com.spiderybook.data.local.entity.HistoryEntity(
                url = episode.url,
                name = "${data.name} - ${episode.name}",
                posterUrl = data.posterUrl ?: "",
                apiName = data.apiName,
                type = data.type?.name,
                playbackPosition = 100,
                duration = 100,
                showTitle = data.name
            )
        )
        
        // Auto-update Favorites Status
        val favorite = localRepository.getFavoriteItem(data.url)
        if (favorite != null) {
            val realEpisodes = data.episodes.filter { it.url != "next_episode" }.reversed()
            val totalEps = realEpisodes.size
            
            // Find true chronological index for this episode to handle 'Episode 0' anomalies uniformly
            val chronologicalIndex = Math.max(1, realEpisodes.indexOf(episode) + 1)
            val maxWatched = Math.max(favorite.watchedEpisodes, chronologicalIndex)
            
            // If the user has watched the maximum number of episodes, mark as Completed, otherwise Watching. Do not mark as Completed if the anime is still airing (next_episode exists).
            val newStatus = when {
                totalEps > 0 && maxWatched >= totalEps && !data.episodes.any { it.url == "next_episode" } -> "Completed"
                maxWatched > 0 -> "Watching"
                else -> "Want to Watch"
            }
            
            localRepository.insertFavorite(
                favorite.copy(
                    watchedEpisodes = maxWatched,
                    totalEpisodes = totalEps,
                    status = newStatus
                )
            )
        }
    }
    
    fun markAllEpisodesAsSeen(data: LoadResponse) = launchIO {
        val realEpisodes = data.episodes.filter { it.url != "next_episode" }
        if (realEpisodes.isNotEmpty()) {
            val highestEpisode = realEpisodes.maxByOrNull { it.episode ?: 0 } ?: realEpisodes.first()
            saveEpisodeProgress(data, highestEpisode)
        }
    }
    
    fun deleteEpisodeProgress(url: String) = launchIO {
        localRepository.deleteHistory(url)
    }
    
    fun addToFavorites(originalUrl: String, currentItem: LoadResponse) = launchIO {
        localRepository.insertFavorite(
            com.spiderybook.data.local.entity.FavoriteEntity(
                url = originalUrl,
                name = currentItem.name,
                posterUrl = currentItem.posterUrl ?: "",
                apiName = currentItem.apiName,
                type = currentItem.type?.name,
                totalEpisodes = currentItem.episodes.count { it.url != "next_episode" }
            )
        )
    }
    
    fun removeFromFavorites(url: String, name: String) = launchIO {
        localRepository.deleteFavorite(url, name)
    }
}
