package com.goforer.phogal.data.repository.gallery

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.goforer.phogal.data.datasource.local.LocalDataSource
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
 * Room-backed (SSOT / offline-first) implementation of [PhotosRepository].
 *
 * Data flow — Unidirectional, database in the middle:
 *
 *   RestAPI.getPhotos (suspend) ──▶ [PhotoFeedRemoteMediator] ──▶ Room `photo_feed`
 *                                                                    │ observable PagingSource
 *                                                                    ▼
 *                                              Pager.flow ──▶ ViewModel ──▶ View
 *
 * The Pager reads exclusively from [PhogalDatabase]; the network is only a
 * writer that keeps the DB fresh. With no connectivity, the cached search feed
 * keeps rendering and the failed refresh surfaces as a `LoadState.Error`.
 */
@Singleton
class PhotosRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val api: RestAPI,
    private val database: PhogalDatabase
) : PhotosRepository {
    override fun getSearchWords(): Flow<List<String>> = localDataSource.searchWordsFlow

    @OptIn(ExperimentalPagingApi::class)
    override fun search(query: String, pageSize: Int): Flow<PagingData<Photo>> {
        val feedKey = PhotoFeedEntity.searchFeedKey(query)

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
                    api.getPhotos(keyword = query, page = page, perPage = perPage)
                }.mapSuccess { response ->
                    val photos = response.results
                    val endReached = photos.isEmpty() || page >= response.totalPages
                    photos to endReached
                }
            },
            // SSOT: the UI is fed from Room, never directly from Retrofit.
            pagingSourceFactory = { database.photoFeedDao().pagingSource(feedKey) }
        ).flow.map { pagingData -> pagingData.map { entity -> entity.photo } }
    }

    override suspend fun setSearchWords(words: List<String>) = localDataSource.setSearchWords(words)
}
