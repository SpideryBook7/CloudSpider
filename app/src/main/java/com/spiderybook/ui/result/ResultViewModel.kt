package com.spiderybook.ui.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.spiderybook.data.repository.LoadRepository
import com.spiderybook.domain.model.LoadResponse
import com.spiderybook.ui.common.BaseViewModel
import com.spiderybook.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val loadRepository: LoadRepository
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
}
