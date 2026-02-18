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

    // Favorites Logic
    fun isFavorite(url: String) = localRepository.isFavorite(url).asLiveData()

    fun toggleFavorite(currentItem: LoadResponse?) = launchIO {
        if (currentItem == null) return@launchIO
        
        val url = currentItem.url
        // Check if exists (snapshot) to toggle
        // Ideally we rely on the UI state, but here we can check or just delete/insert based on current state
        // For simplicity, let's just expose insert/delete and let UI decide or handle check here
        // But since we are inside a coroutine, we can't easily peek LiveData. 
        // Let's rely on the repository's Flow or just try to delete, if 0 deleted then insert? 
        // No, Room delete returns unit or count.
        // Let's just expose insert and delete.
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
