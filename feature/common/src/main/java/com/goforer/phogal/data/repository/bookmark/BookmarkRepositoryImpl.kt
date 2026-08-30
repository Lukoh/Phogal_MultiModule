package com.goforer.phogal.data.repository.bookmark

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.goforer.phogal.data.datasource.local.LocalDataSource
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.data.repository.bookmark.paging.BookmarkPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val localDataSource: LocalDataSource
) : BookmarkRepository {
    override fun getBookmarkList(): Flow<List<Picture>> = localDataSource.bookmarkedPhotosFlow

    override fun bookmarks(bookmarks: List<Picture>, pageSize: Int): Flow<PagingData<Picture>> {
        val bookmarkPagingFlow = Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize * 2,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { BookmarkPagingSource(bookmarks) }
        ).flow

        return bookmarkPagingFlow
    }

    override suspend fun toggleBookmarkPhoto(bookmarkedPhoto: Picture) {
        localDataSource.toggleBookmarkPhoto(bookmarkedPhoto)
    }

    override fun isPhotoBookmarkedFlow(photo: Picture): Flow<Boolean> = localDataSource.isPhotoBookmarkedFlow(photo)

    override fun isPhotoBookmarkedFlow(id: String): Flow<Boolean> = localDataSource.isPhotoBookmarkedFlow(id)
}