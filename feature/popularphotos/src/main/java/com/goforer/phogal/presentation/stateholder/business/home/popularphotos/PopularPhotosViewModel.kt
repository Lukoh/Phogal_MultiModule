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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PopularPhotosViewModel @Inject constructor(
    private val popularPhotosRepository: PopularPhotosRepository
) : ViewModel() {
    private val _orderBy = MutableStateFlow(POPULAR)
    val orderBy: StateFlow<String> = _orderBy.asStateFlow()

    private val _orderSessionId = MutableStateFlow(0)
    val orderSessionId: StateFlow<Int> = _orderSessionId.asStateFlow()

    private val _photos = MutableStateFlow<PagingData<Photo>>(PagingData.empty())

    /**
     * Stream of paged photos.
     * Managed manually to ensure PagingData is cleared immediately when the
     * sort order changes, preventing stale data from flashing on screen.
     */
    val photos: StateFlow<PagingData<Photo>> = _photos.asStateFlow()

    init {
        viewModelScope.launch {
            _orderBy.collectLatest { order ->
                // 1. Clear current photos IMMEDIATELY to prevent flickering
                _photos.value = PagingData.empty()

                // 2. Update session state to trigger UI resets (LazyListState, etc.)
                _orderSessionId.value++

                // 3. Start a new paged stream
                popularPhotosRepository.popularPhotos(orderBy = order, pageSize = PAGE_SIZE)
                    .cachedIn(viewModelScope)
                    .collect { pagingData ->
                        _photos.value = pagingData
                    }
            }
        }
    }

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
