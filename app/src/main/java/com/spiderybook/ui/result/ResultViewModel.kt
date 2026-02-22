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
    private val localRepository: com.spiderybook.data.repository.LocalRepository
) : BaseViewModel() {

    private val _result = MutableLiveData<Resource<LoadResponse>>()
    val result: LiveData<Resource<LoadResponse>> = _result

    fun load(apiName: String, url: String) = launchIO {
        _result.setLoading()
        val data = loadRepository.load(apiName, url)
        if (data != null) {
            _result.setSuccess(data)
        } else {
            _result.setError("Failed to load details")
        }
    }

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
