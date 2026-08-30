package com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.data.repository.bookmark.BookmarkRepository
import com.goforer.phogal.di.dispatcher.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val photos: StateFlow<List<Picture>> = bookmarkRepository.getBookmarkList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val bookmarkedPictures: StateFlow<PagingData<Picture>> = photos
        .flatMapLatest { photos ->
            bookmarkRepository.bookmarks(photos, pageSize = PAGE_SIZE)
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PagingData.empty()
        )

    /**
     * Toggles the bookmark status for [picture].
     */
    fun setBookmarkPicture(picture: Picture) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                bookmarkRepository.toggleBookmarkPhoto(picture)
            }
        }
    }

    /**
     * Existence check for a photo.
     * Note: View should ideally use [isPhotoBookmarkedFlow] to stay reactive.
     */
    fun isPhotoBookmarked(id: String): Boolean {
        return photos.value.any { it.id == id }
    }

    fun isPhotoBookmarked(picture: Picture): Boolean {
        return photos.value.any { it.id == picture.id }
    }

    fun isPhotoBookmarkedFlow(id: String): Flow<Boolean> = bookmarkRepository.isPhotoBookmarkedFlow(id)

    private companion object {
        const val PAGE_SIZE = 20
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
