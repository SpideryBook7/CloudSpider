package com.spiderybook.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spiderybook.data.repository.SearchRepository
import com.spiderybook.domain.model.SearchResponse
import com.spiderybook.ui.common.BaseViewModel
import com.spiderybook.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : BaseViewModel() {

    private val _searchResults = MutableLiveData<Resource<List<SearchResponse>>>()
    val searchResults: LiveData<Resource<List<SearchResponse>>> = _searchResults

    fun search(apiName: String, query: String) = launchIO {
        _searchResults.setLoading()
        val results = searchRepository.search(apiName, query)
        if (results != null) {
            _searchResults.setSuccess(results)
        } else {
            _searchResults.setError("No results found")
        }
    }
}
