package com.spiderybook.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spiderybook.data.repository.SearchRepository
import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.plugins.PluginManager
import com.spiderybook.ui.common.BaseViewModel
import com.spiderybook.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val pluginManager: PluginManager
) : BaseViewModel() {

    private val _searchResults = MutableLiveData<Resource<List<SearchResponse>>>()
    val searchResults: LiveData<Resource<List<SearchResponse>>> = _searchResults

    // If apiName is null/empty, use the first available provider
    fun search(apiName: String?, query: String) = launchIO {
        _searchResults.postValue(Resource.Loading)
        
        val targetApiName = if (apiName.isNullOrEmpty()) {
            pluginManager.apis.firstOrNull()?.name
        } else {
            apiName
        }

        if (targetApiName == null) {
             _searchResults.postValue(Resource.Error("No providers available"))
             return@launchIO
        }

        // Use direct plugin search if repository doesn't support generic search yet
        // OR reuse repository if it supports it. Assuming repo is fine.
        // But previously I saw SearchRepository might be simple. 
        // Let's safe-guard by using pluginManager directly if needed, but let's try repo first.
        // Actually, user said "fix search". Previous code hardcoded "Test Provider".
        // Let's use the found provider.
        
        val results = searchRepository.search(targetApiName, query)
        if (results != null) {
            _searchResults.postValue(Resource.Success(results))
        } else {
            _searchResults.postValue(Resource.Error("No results found"))
        }
    }
}
