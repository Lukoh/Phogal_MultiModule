package com.goforer.phogal.data.repository.common.photo.info

import com.goforer.phogal.data.datasource.local.room.dao.PictureDao
import com.goforer.phogal.data.datasource.local.room.entity.PictureEntity
import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.datasource.network.api.RestAPI
import com.goforer.phogal.data.datasource.network.safeApiCall
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.di.dispatcher.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed (SSOT) implementation of [PictureRepository].
 *
 * Built with the **`flow { }` builder** on top of a Room observable query:
 *
 *   flow {
 *       emit(cache)                      // ① instant render from Room, if present
 *       network → Room upsert            // ② refresh writes to the DB, not the UI
 *       emitAll(dao.observePicture(id))  // ③ DB is the only emission source from here on
 *   }
 *
 * Because step ③ hands the stream over to Room's observable query, every later
 * mutation of the row (a like/unlike patch, a future refresh, …) automatically
 * flows to the ViewModel — the database is the single source of truth, and the
 * screen keeps working with cached data when the network is unavailable.
 */
@Singleton
class PictureRepositoryImpl @Inject constructor(
    private val api: RestAPI,
    private val pictureDao: PictureDao,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher
) : PictureRepository {

    override fun getPictureStream(id: String): Flow<NetworkResult<Picture>> = flow {
        // ① Cache-first: show what we have before touching the network.
        val cached = pictureDao.getPicture(id)
        cached?.let { emit(NetworkResult.Success(it.picture)) }

        // ② Refresh: network result is *persisted*, never emitted directly.
        when (val result = safeApiCall { api.getPhoto(id = id) }) {
            is NetworkResult.Success -> pictureDao.upsert(PictureEntity.of(result.data))
            is NetworkResult.Error -> if (cached == null) emit(result)
            is NetworkResult.Exception -> if (cached == null) emit(result)
            is NetworkResult.Empty -> if (cached == null) emit(NetworkResult.Empty)
        }

        // ③ SSOT hand-over: from now on the Room observable query drives the UI.
        emitAll(
            pictureDao.observePicture(id)
                .filterNotNull()
                .map { entity -> NetworkResult.Success(entity.picture) as NetworkResult<Picture> }
                .distinctUntilChanged()
        )
    }.flowOn(ioDispatcher)
}
