package com.goforer.phogal.presentation.stateholder.business.home.popularphotos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.repository.popularphotos.PopularPhotosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

@HiltViewModel
class PopularPhotosViewModel @Inject constructor(
    private val popularPhotosRepository: PopularPhotosRepository
) : ViewModel() {
    private val _orderBy = MutableStateFlow(POPULAR)
    val orderBy: StateFlow<String> = _orderBy.asStateFlow()

    private val _orderSessionId = MutableStateFlow(0)
    val orderSessionId: StateFlow<Int> = _orderSessionId.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val photos: StateFlow<PagingData<Photo>> = combine(_orderBy, _orderSessionId) { order, sessionId ->
        order to sessionId
    }
        .transformLatest { (order, _) ->
            // Clear cache via suspend function inside the coroutine stream context
            // BEFORE collecting the new Pager flow. This creates an airtight synchronous gap
            // that forces Paging 3's initial factory access to observe 0 items.
            popularPhotosRepository.clearCache(order)
            popularPhotosRepository.popularPhotos(orderBy = order, pageSize = PAGE_SIZE).collect { pagingData ->
                emit(pagingData)
            }
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = PagingData.empty()
        )

    fun onOrderChanged(newOrder: String) {
        if (_orderBy.value != newOrder) {
            _orderBy.value = newOrder
            _orderSessionId.value++
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
