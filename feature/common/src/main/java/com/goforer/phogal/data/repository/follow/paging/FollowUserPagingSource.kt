package com.goforer.phogal.data.repository.follow.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import kotlin.coroutines.cancellation.CancellationException

/**
 * Paging source for followed users stored locally.
 *
 * This source pages over an in-memory [List] of [User] objects.
 * It uses offset-based indexing to ensure that variable load sizes (e.g., initialLoadSize vs. pageSize)
 * do not cause duplicate items to be fetched or page boundary skips during prepends after a refresh.
 *
 * Note: [com.goforer.phogal.data.datasource.network.safeApiCall] is not used here
 * because this source operates on local data and does not produce HTTP responses.
 */
class FollowUserPagingSource(
    private val followedUsers: List<User>,
    private val pageSize: Int = 20
) : PagingSource<Int, User>() {

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(state.config.pageSize)
                ?: anchorPage?.nextKey?.minus(state.config.pageSize)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val offset = params.key ?: 0
        val loadSize = params.loadSize

        if (followedUsers.isEmpty()) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        }

        return try {
            val fromIndex = offset.coerceAtLeast(0)
            val toIndex = (fromIndex + loadSize).coerceAtMost(followedUsers.size)

            val data = if (fromIndex < followedUsers.size) {
                followedUsers.subList(fromIndex, toIndex)
            } else {
                emptyList()
            }

            // For Prepend requests, step back by loadSize.
            // For Refresh/Append requests at a non-zero offset, step back by pageSize so subsequent
            // prepend requests fetch the contiguous preceding page without leaving gaps (e.g., skipping 40..59).
            val step = if (params is LoadParams.Prepend) loadSize else pageSize
            val prevKey = if (fromIndex <= 0) null else (fromIndex - step).coerceAtLeast(0)
            val nextKey = if (toIndex >= followedUsers.size) null else toIndex

            LoadResult.Page(
                data = data,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            LoadResult.Error(t)
        }
    }
}
