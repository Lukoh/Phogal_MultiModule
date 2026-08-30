package com.goforer.phogal.presentation.stateholder.business.home.popularphotos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.repository.popularphotos.PopularPhotosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PopularPhotosViewModel @Inject constructor(
    private val popularPhotosRepository: PopularPhotosRepository
) : ViewModel() {
    private val _orderBy = MutableStateFlow(POPULAR)
    val orderBy: StateFlow<String> = _orderBy.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val photos: StateFlow<PagingData<Photo>> = _orderBy
        .flatMapLatest { order ->
            popularPhotosRepository.popularPhotos(orderBy = order, pageSize = PAGE_SIZE)
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PagingData.empty()
        )

    fun onOrderChanged(newOrder: String) {
        if (_orderBy.value != newOrder) {
            _orderBy.value = newOrder
        }
    }

    companion object {
        const val POPULAR = "popular"
        const val LATEST = "latest"
        const val OLDEST = "oldest"

        private const val PAGE_SIZE = 20
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
