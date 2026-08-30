package com.goforer.phogal.data.repository.common.photo.like

import com.goforer.phogal.data.datasource.local.room.dao.PictureDao
import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.datasource.network.api.RestAPI
import com.goforer.phogal.data.datasource.network.safeApiCall
import com.goforer.phogal.data.model.remote.response.gallery.photo.like.LikeResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Like/unlike with SSOT propagation.
 *
 * After a successful REST mutation the cached Room row is patched in place
 * (read-modify-write on the `picture` table). Room's observable query then
 * re-emits the row, so every screen currently collecting
 * [com.goforer.phogal.data.repository.common.photo.info.PictureRepository.getPictureStream]
 * updates automatically — no manual in-memory state patching in ViewModels.
 */
@Singleton
class PictureLikeRepositoryImpl @Inject constructor(
    private val api: RestAPI,
    private val pictureDao: PictureDao
) : PictureLikeRepository {

    override suspend fun like(pictureId: String): NetworkResult<LikeResponse> {
        val result = safeApiCall { api.postLike(id = pictureId) }
        if (result is NetworkResult.Success || result is NetworkResult.Empty) {
            patchLikedByUser(pictureId, liked = true)
        }
        return result
    }

    override suspend fun unlike(pictureId: String): NetworkResult<LikeResponse> {
        val result = safeApiCall { api.deleteLike(id = pictureId) }
        if (result is NetworkResult.Success || result is NetworkResult.Empty) {
            patchLikedByUser(pictureId, liked = false)
        }
        return result
    }

    /** Read-modify-write patch of the cached detail row — triggers the DAO Flow. */
    private suspend fun patchLikedByUser(pictureId: String, liked: Boolean) {
        val entity = pictureDao.getPicture(pictureId) ?: return
        if (entity.picture.likedByUser == liked) return
        pictureDao.upsert(entity.copy(picture = entity.picture.copy(likedByUser = liked)))
    }
}
