package com.spiderybook.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spiderybook.data.repository.LoadRepository
import com.spiderybook.plugins.MainAPI
import com.spiderybook.ui.common.BaseViewModel
import com.spiderybook.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val loadRepository: LoadRepository
) : BaseViewModel() {

    private val _links = MutableLiveData<Resource<List<MainAPI.ExtractorLink>>>()
    val links: LiveData<Resource<List<MainAPI.ExtractorLink>>> = _links

    fun loadLinks(apiName: String, data: String) = launchIO {
        _links.setLoading()
        // We need to add loadLinks to Repository first, or access plugin manager directly
        // For simplicity, let's update LoadRepository
        val links = mutableListOf<MainAPI.ExtractorLink>()
        val success = loadRepository.loadLinks(apiName, data) { link ->
            links.add(link)
        }
        
        if (success && links.isNotEmpty()) {
            _links.setSuccess(links)
        } else {
            _links.setError("Failed to extract links")
        }
    }
}
