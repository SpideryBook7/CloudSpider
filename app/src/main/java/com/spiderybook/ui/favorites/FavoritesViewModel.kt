package com.spiderybook.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.spiderybook.data.repository.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    repository: LocalRepository
) : ViewModel() {
    val favorites = repository.getFavorites().asLiveData()
}
