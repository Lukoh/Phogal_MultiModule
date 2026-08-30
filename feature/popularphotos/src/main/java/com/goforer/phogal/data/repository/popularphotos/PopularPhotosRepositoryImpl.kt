package com.goforer.phogal.data.repository.popularphotos

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.goforer.phogal.data.datasource.local.room.PhogalDatabase
import com.goforer.phogal.data.datasource.local.room.entity.PhotoFeedEntity
import com.goforer.phogal.data.datasource.local.room.mediator.PhotoFeedRemoteMediator
import com.goforer.phogal.data.datasource.network.api.RestAPI
import com.goforer.phogal.data.datasource.network.mapSuccess
import com.goforer.phogal.data.datasource.network.safeApiCall
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed (SSOT / offline-first) implementation of [PopularPhotosRepository].
 *
 * The popular feed is the app's landing content, so offline-first matters most
 * here: `RestAPI.getPopularPhotos` fills the Room `photo_feed` table through the
 * [PhotoFeedRemoteMediator], and the UI pages exclusively out of the database.
 * A cold start in airplane mode renders the last cached feed instantly.
 */
@Singleton
class PopularPhotosRepositoryImpl @Inject constructor(
    private val api: RestAPI,
    private val database: PhogalDatabase
) : PopularPhotosRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun popularPhotos(orderBy: String, pageSize: Int): Flow<PagingData<Photo>> {
        val feedKey = PhotoFeedEntity.popularFeedKey(orderBy)

        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize * 2,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            remoteMediator = PhotoFeedRemoteMediator(
                feedKey = feedKey,
                pageSize = pageSize,
                database = database
            ) { page, perPage ->
                safeApiCall {
                    api.getPopularPhotos(orderBy = orderBy, page = page, perPage = perPage)
                }.mapSuccess { photos ->
                    val endReached = photos.isEmpty() || photos.size < perPage
                    photos to endReached
                }
            },
            pagingSourceFactory = { database.photoFeedDao().pagingSource(feedKey) }
        ).flow.map { pagingData -> pagingData.map { entity -> entity.photo } }
    }
}
