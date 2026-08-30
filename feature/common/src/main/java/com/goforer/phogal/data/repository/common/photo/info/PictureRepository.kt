package com.goforer.phogal.data.repository.common.photo.info

import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import kotlinx.coroutines.flow.Flow

interface PictureRepository {
    /**
     * SSOT stream of a single picture.
     *
     * Emission contract (offline-first):
     *  1. If a cached row exists in Room, it is emitted immediately.
     *  2. The network is refreshed once; a success is **written to Room only**
     *     (never emitted directly).
     *  3. The Room observable query then streams every DB change — the fresh
     *     network payload, like/unlike patches, and any later writers.
     *  4. A network failure is emitted as [NetworkResult.Error] / [NetworkResult.Exception]
     *     only when there is no cached row to show (otherwise stale data wins).
     *
     * @param id Unsplash photo id
     */
    fun getPictureStream(id: String): Flow<NetworkResult<Picture>>
}
