package com.goforer.phogal.data.repository.common.photo.like

import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.model.remote.response.gallery.photo.like.LikeResponse

interface PictureLikeRepository {

    /** Mark the picture as liked by the current user. */
    suspend fun like(pictureId: String): NetworkResult<LikeResponse>

    /** Undo a like on the picture. */
    suspend fun unlike(pictureId: String): NetworkResult<LikeResponse>
}
