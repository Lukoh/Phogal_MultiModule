package com.goforer.phogal.data.repository.bookmark

import androidx.paging.PagingData
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BookmarkRepository {
    fun getBookmarkList(): Flow<List<Picture>>

    /**
     * @param bookmarks     Bookmarked the photo list (non-blank)
     * @param pageSize  page size for [androidx.paging.PagingConfig]; also used as initial load size
     */
    fun bookmarks(bookmarks: List<Picture>, pageSize: Int): Flow<PagingData<Picture>>

    suspend fun toggleBookmarkPhoto(bookmarkedPhoto: Picture)

    fun isPhotoBookmarkedFlow(photo: Picture): Flow<Boolean>

    fun isPhotoBookmarkedFlow(id: String): Flow<Boolean>
}