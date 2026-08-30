package com.goforer.phogal.data.repository.bookmark.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import kotlin.coroutines.cancellation.CancellationException

/**
 * Paging source for bookmarked photos stored locally.
 *
 * This source pages over an in-memory [List] of [Picture] objects.
 * It uses 1-based indexing for consistency with network-based paging sources.
 *
 * Note: [com.goforer.phogal.data.datasource.network.safeApiCall] is not used here
 * because this source operates on local data and does not produce HTTP responses.
 */
class BookmarkPagingSource(
    private val pictures: List<Picture>
) : PagingSource<Int, Picture>() {

    override fun getRefreshKey(state: PagingState<Int, Picture>): Int? {
        return state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor) ?: return null
            page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Picture> {
        val page = params.key ?: STARTING_PAGE
        val pageSize = params.loadSize

        if (pictures.isEmpty()) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        }

        return try {
            val fromIndex = (page - 1) * pageSize
            val toIndex = (fromIndex + pageSize).coerceAtMost(pictures.size)

            val data = if (fromIndex in pictures.indices) {
                pictures.subList(fromIndex, toIndex)
            } else {
                emptyList()
            }

            LoadResult.Page(
                data = data,
                prevKey = if (page == STARTING_PAGE) null else page - 1,
                nextKey = if (toIndex >= pictures.size) null else page + 1
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
