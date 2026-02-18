package com.spiderybook.ui.browse

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.plugins.providers.AnimeFlvProvider
import com.spiderybook.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val provider: AnimeFlvProvider
) : ViewModel() {

    private val _items = MutableLiveData<Resource<List<SearchResponse>>>()
    val items: LiveData<Resource<List<SearchResponse>>> = _items

    private val _currentList = mutableListOf<SearchResponse>()
    private var currentPage = 1
    private var isLastPage = false
    private var isLoading = false

    init {
        loadNextPage()
    }

    fun loadNextPage() {
        if (isLoading || isLastPage) return

        isLoading = true
        // Show loading only on first page or handled by adapter footer?
        // For simplicity, we just post current list + loading state if needed.
        if (currentPage == 1) {
            _items.postValue(Resource.Loading)
        }

        viewModelScope.launch {
            try {
                android.util.Log.d("BrowseViewModel", "Fetching page $currentPage...")
                val newItems = provider.getBrowsePage(currentPage)
                android.util.Log.d("BrowseViewModel", "Fetched ${newItems.size} items")
                if (newItems.isNotEmpty()) {
                    // Filter duplicates just in case
                    val distinctItems = newItems.filter { newItem -> 
                        _currentList.none { it.url == newItem.url } 
                    }
                    
                    if (distinctItems.isEmpty() && newItems.isNotEmpty()) {
                        // If we got items but all were duplicates, maybe we are looping?
                        // Just increment anyway.
                    }
                    
                    _currentList.addAll(distinctItems)
                    _items.postValue(Resource.Success(_currentList.toList()))
                    currentPage++
                } else {
                    isLastPage = true
                    if (currentPage == 1) {
                         _items.postValue(Resource.Error("No content found"))
                    }
                }
            } catch (e: Exception) {
                if (currentPage == 1) {
                    _items.postValue(Resource.Error(e.message ?: "Unknown error"))
                }
            } finally {
                isLoading = false
            }
        }
    }
}
