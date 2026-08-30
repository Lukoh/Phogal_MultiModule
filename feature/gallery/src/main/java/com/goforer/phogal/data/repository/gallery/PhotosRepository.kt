package com.goforer.phogal.data.repository.gallery

import androidx.paging.PagingData
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import kotlinx.coroutines.flow.Flow

/**
 * Search photos on Unsplash by keyword, paged.
 *
 * Consumers should typically call [search] from a ViewModel-scoped coroutine and
 * forward the resulting [Flow] through `cachedIn(viewModelScope)` before exposing it
 * to the UI layer as a `StateFlow<PagingData<Photo>>`.
 */
interface PhotosRepository {
    fun getSearchWords(): Flow<List<String>>

    /**
     * @param query     user keyword (non-blank)
     * @param pageSize  page size for [androidx.paging.PagingConfig]; also used as initial load size
     */
    fun search(query: String, pageSize: Int): Flow<PagingData<Photo>>

    /**
     * @param words  th keyword list (non-blank)
     */
    suspend fun setSearchWords(words: List<String>)
}
