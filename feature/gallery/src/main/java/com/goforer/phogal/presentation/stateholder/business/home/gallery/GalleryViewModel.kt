package com.goforer.phogal.presentation.stateholder.business.home.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.repository.gallery.PhotosRepository
import com.goforer.phogal.di.dispatcher.IoDispatcher
import com.goforer.phogal.presentation.stateholder.business.home.gallery.GalleryViewModel.Companion.MAX_HISTORY_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val photosRepository: PhotosRepository,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    private data class SearchRequest(val query: String, val sessionId: Int)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchRequest = MutableStateFlow(SearchRequest("", 0))

    val searchingQuery: StateFlow<String> = _searchRequest
        .map { it.query }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val searchSessionId: StateFlow<Int> = _searchRequest
        .map { it.sessionId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _queryTrigger = MutableSharedFlow<QueryUpdate>(replay = 0, extraBufferCapacity = 1)

    /**
     * Recent search keywords, newest first.
     */
    val recentWords: StateFlow<List<String>> = photosRepository.getSearchWords()
        .map { it.reversed() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = emptyList()
        )

    /**
     * Stream of paged photos. Switches every time [searchingQuery] or [searchSessionId] changes.
     * Atomically observes [_searchRequest] to prevent race conditions or double triggers.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val photos: StateFlow<PagingData<Photo>> = _searchRequest
        .filter { it.query.isNotBlank() }
        .transformLatest { request ->
            // Clear stale PagingData immediately so UI doesn't briefly display
            // previous search results while new query is fetching.
            emit(PagingData.empty())
            photosRepository.clearCache(request.query)
            photosRepository.search(request.query, PAGE_SIZE).collect { pagingData ->
                emit(pagingData)
            }
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = PagingData.empty()
        )

    init {
        viewModelScope.launch {
            _queryTrigger
                .collectLatest { update ->
                    if (update is QueryUpdate.Typing) {
                        delay(DEBOUNCE_MS.milliseconds)
                    }

                    val trimmedQuery = update.query.trim()
                    if (trimmedQuery.isNotBlank()) {
                        _searchRequest.value = SearchRequest(
                            query = trimmedQuery,
                            sessionId = _searchRequest.value.sessionId + 1
                        )
                    }
                }
        }
    }

    private val _events = MutableSharedFlow<GalleryUiEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<GalleryUiEvent> = _events.asSharedFlow()

    fun onQueryChanged(newQuery: String, immediate: Boolean = false) {
        _query.value = newQuery
        val update = if (immediate) QueryUpdate.Direct(newQuery) else QueryUpdate.Typing(newQuery)
        _queryTrigger.tryEmit(update)
    }

    /**
     * Commits the current query to local search history, capped at [MAX_HISTORY_SIZE].
     * I/O is dispatched off the main thread.
     */
    fun commitSearch(keyword: String = _query.value) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return

        onQueryChanged(trimmed, immediate = true)
        viewModelScope.launch {
            withContext(ioDispatcher) {
                val currentKeywords = recentWords.value.reversed().toMutableList()

                if (trimmed in currentKeywords) return@withContext
                if (currentKeywords.size >= MAX_HISTORY_SIZE) currentKeywords.removeAt(0)
                currentKeywords += trimmed
                
                photosRepository.setSearchWords(currentKeywords)
            }
            _events.tryEmit(GalleryUiEvent.SearchCommitted(trimmed))
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val DEBOUNCE_MS = 300L
        const val MAX_HISTORY_SIZE = 7
        const val STOP_TIMEOUT_MS = 5_000L
    }

    private sealed interface QueryUpdate {
        val query: String
        data class Typing(override val query: String) : QueryUpdate
        data class Direct(override val query: String) : QueryUpdate
    }
}

/** One-shot UI events from [GalleryViewModel]. */
sealed interface GalleryUiEvent {
    data class SearchCommitted(val keyword: String) : GalleryUiEvent
}
