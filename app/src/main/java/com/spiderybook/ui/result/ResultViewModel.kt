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
            
            // OPTIMIZATION: Only hit TMDB for explicitly supported providers to save bandwidth and CPU
            if (apiName.contains("pelisplus", ignoreCase = true)) {
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
                android.util.Log.d("TMDB_DEBUG", "Skipping TMDB fetch for unsupported provider: $apiName")
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
            val bestLink = links.firstOrNull { it.url.contains(".mp4") } ?: links.first()
            downloadManager.download(bestLink.url, fileName)
            _downloadStatus.postValue(Resource.Success("Descargando: $fileName"))
        } else {
            _downloadStatus.postValue(Resource.Error("Error al obtener el enlace de descarga para $fileName"))
        }
    }

    // Favorites & History Logic
    fun isFavorite(url: String) = localRepository.isFavorite(url).asLiveData()
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
    }
    
    fun deleteEpisodeProgress(url: String) = launchIO {
        localRepository.deleteHistory(url)
    }
    
    fun addToFavorites(currentItem: LoadResponse) = launchIO {
        localRepository.insertFavorite(
            com.spiderybook.data.local.entity.FavoriteEntity(
                url = currentItem.url,
                name = currentItem.name,
                posterUrl = currentItem.posterUrl ?: "",
                apiName = currentItem.apiName,
                type = currentItem.type?.name
            )
        )
    }
    
    fun removeFromFavorites(url: String) = launchIO {
        localRepository.deleteFavorite(url)
    }
}
