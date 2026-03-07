package com.spiderybook.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.spiderybook.data.repository.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel() {
    val favorites = repository.getFavorites().asLiveData()

    fun deleteFavoriteItems(urls: List<String>) {
        viewModelScope.launch {
            repository.deleteFavoriteItems(urls)
        }
    }
}
