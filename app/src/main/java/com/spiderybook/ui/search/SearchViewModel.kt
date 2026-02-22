package com.spiderybook.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spiderybook.data.local.DataStoreManager
import com.spiderybook.data.repository.SearchRepository
import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.domain.model.TvType
import com.spiderybook.plugins.PluginManager
import com.spiderybook.ui.common.BaseViewModel
import com.spiderybook.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val pluginManager: PluginManager,
    private val dataStoreManager: DataStoreManager
) : BaseViewModel() {

    private val _searchResults = MutableLiveData<Resource<List<SearchResponse>>>()
    val searchResults: LiveData<Resource<List<SearchResponse>>> = _searchResults

    private val _topSearches = MutableLiveData<List<SearchResponse>>()
    val topSearches: LiveData<List<SearchResponse>> = _topSearches

    private val _genres = MutableLiveData<List<String>>()
    val genres: LiveData<List<String>> = _genres

    init {
        loadDefaultContent()
    }

    private var currentApiName: String? = null

    private fun loadDefaultContent() = launchIO {
        dataStoreManager.readString(DataStoreManager.API_URL, "").collect { savedProvider ->
            val preferred = if (savedProvider.isEmpty()) pluginManager.apis.firstOrNull()?.name else savedProvider
            
            // Re-load when the provider changes
            val targetApiName = preferred ?: pluginManager.apis.firstOrNull()?.name
            if (targetApiName != null) {
                currentApiName = targetApiName
                
                // Nested suspending calls must be inside coroutine or properly handled.
                // launchIO already provides CoroutineScope so we can call suspend functions if they are suspended.
                // But getGenres and getTopSearches are suspend functions mapped to Repository.
                val providerGenres = searchRepository.getGenres(targetApiName)
                _genres.postValue(providerGenres)
                
                val providerTopSearches = searchRepository.getTopSearches(targetApiName)
                _topSearches.postValue(providerTopSearches)
            }
        }
    }

    private var currentPage = 1
    private var currentQuery = ""
    private var isSearching = false
    private val accumulatedResults = mutableListOf<SearchResponse>()

    // If apiName is null/empty, use the currently active provider
    fun search(apiName: String?, query: String, isLoadMore: Boolean = false) = launchIO {
        if (isLoadMore) {
            if (isSearching || query != currentQuery) return@launchIO
            currentPage++
        } else {
            currentPage = 1
            currentQuery = query
            accumulatedResults.clear()
            _searchResults.postValue(Resource.Loading)
        }
        
        isSearching = true
        
        val targetApiName = if (apiName.isNullOrEmpty()) {
            currentApiName ?: pluginManager.apis.firstOrNull()?.name
        } else {
            apiName
        }

        if (targetApiName == null) {
             _searchResults.postValue(Resource.Error("No providers available"))
             isSearching = false
             return@launchIO
        }

        val results = searchRepository.search(targetApiName, currentQuery, currentPage)
        
        if (results != null && results.isNotEmpty()) {
            accumulatedResults.addAll(results)
            _searchResults.postValue(Resource.Success(accumulatedResults.toList()))
        } else {
            if (!isLoadMore) {
                _searchResults.postValue(Resource.Error("No results found"))
            }
            // If it's load more and no results, we just leave it as is or show a toast
        }
        
        isSearching = false
    }
}
