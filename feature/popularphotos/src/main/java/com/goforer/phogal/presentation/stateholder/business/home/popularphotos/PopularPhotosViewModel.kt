package com.goforer.phogal.presentation.stateholder.business.home.popularphotos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.repository.popularphotos.PopularPhotosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

@HiltViewModel
class PopularPhotosViewModel @Inject constructor(
    private val popularPhotosRepository: PopularPhotosRepository
) : ViewModel() {
    private data class OrderRequest(val order: String, val sessionId: Int)

    private val _orderRequest = MutableStateFlow(OrderRequest(POPULAR, 0))

    val orderBy: StateFlow<String> = _orderRequest
        .map { it.order }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, POPULAR)

    val orderSessionId: StateFlow<Int> = _orderRequest
        .map { it.sessionId }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val photos: StateFlow<PagingData<Photo>> = _orderRequest
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
        val current = _orderRequest.value
        if (current.order != newOrder) {
            _orderRequest.value = OrderRequest(newOrder, current.sessionId + 1)
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
