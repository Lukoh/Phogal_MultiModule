package com.goforer.phogal.presentation.stateholder.business.home.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.repository.gallery.PhotosRepository
import com.goforer.phogal.di.dispatcher.IoDispatcher
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

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
     * Stream of paged photos. Switches every time [query] changes (debounced, distinct).
     * Blank queries are filtered out so the UI's empty state doesn't burn a request.
     */
    val photos: StateFlow<PagingData<Photo>> = _queryTrigger
        .onStart {
            val initial = _query.value
            if (initial.isNotBlank()) emit(QueryUpdate.Direct(initial))
        }
        .transformLatest { update ->
            if (update is QueryUpdate.Typing) {
                delay(DEBOUNCE_MS.milliseconds)
            }
            emit(update.query)
        }
        .distinctUntilChanged()
        .filter { it.isNotBlank() }
        .flatMapLatest { query -> photosRepository.search(query, PAGE_SIZE) }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PagingData.empty()
        )

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
    fun commitSearch() {
        val keyword = _query.value.trim()
        if (keyword.isEmpty()) return

        onQueryChanged(keyword, immediate = true)
        viewModelScope.launch {
            withContext(ioDispatcher) {
                // Get current words from the StateFlow. Note: we use the raw list
                // from the repo (which is reversed in the public recentWords flow).
                // But it's easier to just use recentWords.value and reverse it back
                // if we want to maintain the "append to end" logic, OR just prepend.
                val currentKeywords = recentWords.value.reversed().toMutableList()

                if (keyword in currentKeywords) return@withContext
                if (currentKeywords.size >= MAX_HISTORY_SIZE) currentKeywords.removeAt(0)
                currentKeywords += keyword
                
                photosRepository.setSearchWords(currentKeywords)
            }
            _events.tryEmit(GalleryUiEvent.SearchCommitted(keyword))
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
