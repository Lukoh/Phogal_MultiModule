package com.goforer.phogal.data.datasource.local.room.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.goforer.phogal.data.datasource.local.room.PhogalDatabase
import com.goforer.phogal.data.datasource.local.room.entity.PhotoFeedEntity
import com.goforer.phogal.data.datasource.local.room.entity.RemoteKeyEntity
import com.goforer.phogal.data.datasource.network.BackendException
import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * One RemoteMediator implementation for every positional (page/per_page) photo
 * feed: keyword search, popular photos, and a user's photos. What differs per
 * feed is only the [feedKey] and the [fetchPage] lambda that calls the matching
 * `RestAPI` suspend function.
 *
 * Role in the SSOT architecture: Paging 3 renders **only** what is in the
 * `photo_feed` table (via [com.goforer.phogal.data.datasource.local.room.dao.PhotoFeedDao.pagingSource]).
 * This mediator is the single component allowed to write network pages into
 * that table:
 *
 *  - REFRESH  → fetch page 1, then atomically (in one Room transaction)
 *               clear the feed + replace its rows + reset the remote key.
 *  - APPEND   → fetch `remote_key.next_page`, append rows, advance the key.
 *  - PREPEND  → not supported by the API (forward-only paging) → end of pagination.
 *
 * Offline behaviour: [initialize] skips the initial network refresh while the
 * cache is younger than [cacheTimeoutMs], so a cold start with no connectivity
 * still shows the cached feed instantly. When a later load fails, Paging keeps
 * presenting the cached rows and surfaces the failure as a `LoadState.Error`
 * that the existing UI already renders (retry affordances included).
 */
@OptIn(ExperimentalPagingApi::class)
class PhotoFeedRemoteMediator(
    private val feedKey: String,
    private val pageSize: Int,
    private val database: PhogalDatabase,
    private val cacheTimeoutMs: Long = TimeUnit.MINUTES.toMillis(30),
    private val fetchPage: suspend (page: Int, perPage: Int) -> NetworkResult<Pair<List<Photo>, Boolean>>
) : RemoteMediator<Int, PhotoFeedEntity>() {

    private val photoFeedDao = database.photoFeedDao()
    private val remoteKeyDao = database.remoteKeyDao()

    override suspend fun initialize(): InitializeAction {
        val key = remoteKeyDao.remoteKey(feedKey)
        val cachedCount = photoFeedDao.countFeed(feedKey)
        val isCacheFresh = key != null &&
                (System.currentTimeMillis() - key.lastRefreshedAt) < cacheTimeoutMs

        return if (cachedCount > 0 && isCacheFresh) {
            // Serve straight from Room; no network round-trip on entry.
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PhotoFeedEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> STARTING_PAGE
            // Unsplash list endpoints are forward-only; nothing to prepend.
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val key = remoteKeyDao.remoteKey(feedKey)
                key?.nextPage ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return when (val result = fetchPage(page, pageSize)) {
            is NetworkResult.Success -> {
                val (photos, endReached) = result.data
                val now = System.currentTimeMillis()

                // Atomic swap: readers never observe a half-written feed.
                database.withTransaction {
                    // Upsert the new data first without deleting the existing data.
                    // This completely prevents the list from becoming empty at any point.
                    photoFeedDao.insertAll(photos.map { PhotoFeedEntity.of(feedKey, it, now) })

                    if (loadType == LoadType.REFRESH) {
                        val prefix = feedKey.substringBefore('/') + "/"

                        // Instead of a full deletion, delete only the old data saved before the current insertion time (now).
                        // (This cleans up outdated caches that were not updated because they were missing from the network response.)
                        photoFeedDao.deleteOldFeedsByPrefix(prefix, now)
                        remoteKeyDao.clearByPrefix(prefix)
                    }

                    val previous = remoteKeyDao.remoteKey(feedKey)
                    remoteKeyDao.upsert(
                        RemoteKeyEntity(
                            feedKey = feedKey,
                            nextPage = if (endReached) null else page + 1,
                            lastRefreshedAt = if (loadType == LoadType.REFRESH) {
                                now
                            } else {
                                previous?.lastRefreshedAt ?: now
                            }
                        )
                    )
                }
                MediatorResult.Success(endOfPaginationReached = endReached)
            }

            is NetworkResult.Empty -> {
                database.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        val prefix = feedKey.substringBefore('/') + "/"
                        photoFeedDao.clearFeedsByPrefix(prefix)
                        remoteKeyDao.clearByPrefix(prefix)
                        
                        remoteKeyDao.upsert(
                            RemoteKeyEntity(
                                feedKey = feedKey,
                                nextPage = null,
                                lastRefreshedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
                MediatorResult.Success(endOfPaginationReached = true)
            }

            is NetworkResult.Error -> {
                Timber.w("RemoteMediator[$feedKey] HTTP ${result.code}: ${result.message}")
                MediatorResult.Error(BackendException(result.code, result.message))
            }

            is NetworkResult.Exception -> {
                // Typically IOException in airplane mode — cached rows stay on screen.
                Timber.w(result.throwable, "RemoteMediator[$feedKey] network failure")
                MediatorResult.Error(result.throwable)
            }
        }
    }

    private companion object {
        const val STARTING_PAGE = 1
    }
}
