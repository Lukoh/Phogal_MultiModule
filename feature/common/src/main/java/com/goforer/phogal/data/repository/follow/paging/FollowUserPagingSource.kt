package com.goforer.phogal.data.repository.follow.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import kotlin.coroutines.cancellation.CancellationException

/**
 * Paging source for followed users stored locally.
 *
 * This source pages over an in-memory [List] of [User] objects.
 * It uses 1-based indexing for consistency with network-based paging sources.
 *
 * Note: [com.goforer.phogal.data.datasource.network.safeApiCall] is not used here
 * because this source operates on local data and does not produce HTTP responses.
 */
class FollowUserPagingSource(
    private val followedUsers: List<User>
) : PagingSource<Int, User>() {

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor) ?: return null
            page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val page = params.key ?: STARTING_PAGE
        val pageSize = params.loadSize

        if (followedUsers.isEmpty()) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        }

        return try {
            val fromIndex = (page - 1) * pageSize
            val toIndex = (fromIndex + pageSize).coerceAtMost(followedUsers.size)

            val data = if (fromIndex in followedUsers.indices) {
                followedUsers.subList(fromIndex, toIndex)
            } else {
                emptyList()
            }

            LoadResult.Page(
                data = data,
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (toIndex >= followedUsers.size) null else page + 1
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            LoadResult.Error(t)
        }
    }

    private companion object {
        const val STARTING_PAGE = 1
    }
}
