package com.goforer.phogal.data.repository.common.user.photos

import androidx.paging.PagingData
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import kotlinx.coroutines.flow.Flow

/**
 * Repository for loading all photos uploaded by a specific Unsplash user, paged.
 */
interface UserPhotosRepository {
    /**
     * @param username  Unsplash username, not the display name
     * @param pageSize  page size for [androidx.paging.PagingConfig]
     */
    fun userPhotos(username: String, pageSize: Int): Flow<PagingData<Photo>>
}
