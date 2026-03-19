package com.spiderybook.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.spiderybook.data.repository.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel() {
    
    private val _currentTab = MutableStateFlow("Watching")

    val favorites = repository.getFavorites().combine(_currentTab) { list, tab ->
        list.filter { it.status == tab }
    }.asLiveData()

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun deleteFavoriteItems(urls: List<String>) {
        viewModelScope.launch {
            repository.deleteFavoriteItems(urls)
        }
    }
}
