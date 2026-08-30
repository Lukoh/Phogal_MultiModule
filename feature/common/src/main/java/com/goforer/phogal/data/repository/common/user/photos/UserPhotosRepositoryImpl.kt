package com.goforer.phogal.data.repository.common.user.photos

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
 * Room-backed (SSOT / offline-first) implementation of [UserPhotosRepository].
 *
 * Each user's photo list is cached under its own feed key (`user/<username>`),
 * so revisiting a profile — online or offline — replays the cached feed from
 * Room while the [PhotoFeedRemoteMediator] refreshes it in the background.
 */
@Singleton
class UserPhotosRepositoryImpl @Inject constructor(
    private val api: RestAPI,
    private val database: PhogalDatabase
) : UserPhotosRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun userPhotos(username: String, pageSize: Int): Flow<PagingData<Photo>> {
        val feedKey = PhotoFeedEntity.userFeedKey(username)

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
                    api.getUserPhotos(username = username, page = page, perPage = perPage)
                }.mapSuccess { photos ->
                    val endReached = photos.isEmpty() || photos.size < perPage
                    photos to endReached
                }
            },
            pagingSourceFactory = { database.photoFeedDao().pagingSource(feedKey) }
        ).flow.map { pagingData -> pagingData.map { entity -> entity.photo } }
    }
}
