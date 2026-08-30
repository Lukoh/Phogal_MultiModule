package com.goforer.phogal.di.module

import com.goforer.phogal.data.repository.bookmark.BookmarkRepository
import com.goforer.phogal.data.repository.bookmark.BookmarkRepositoryImpl
import com.goforer.phogal.data.repository.common.photo.info.PictureRepository
import com.goforer.phogal.data.repository.common.photo.info.PictureRepositoryImpl
import com.goforer.phogal.data.repository.common.photo.like.PictureLikeRepository
import com.goforer.phogal.data.repository.common.photo.like.PictureLikeRepositoryImpl
import com.goforer.phogal.data.repository.common.user.photos.UserPhotosRepository
import com.goforer.phogal.data.repository.common.user.photos.UserPhotosRepositoryImpl
import com.goforer.phogal.data.repository.download.PhotoDownloadRepository
import com.goforer.phogal.data.repository.download.PhotoDownloadRepositoryImpl
import com.goforer.phogal.data.repository.follow.FollowUserRepository
import com.goforer.phogal.data.repository.follow.FollowUserRepositoryImpl
import com.goforer.phogal.data.repository.gallery.PhotosRepository
import com.goforer.phogal.data.repository.gallery.PhotosRepositoryImpl
import com.goforer.phogal.data.repository.popularphotos.PopularPhotosRepository
import com.goforer.phogal.data.repository.popularphotos.PopularPhotosRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPhotosRepository(impl: PhotosRepositoryImpl): PhotosRepository

    @Binds
    @Singleton
    abstract fun bindPictureRepository(impl: PictureRepositoryImpl): PictureRepository

    @Binds
    @Singleton
    abstract fun bindPictureLikeRepository(impl: PictureLikeRepositoryImpl): PictureLikeRepository

    @Binds
    @Singleton
    abstract fun bindUserPhotosRepository(impl: UserPhotosRepositoryImpl): UserPhotosRepository

    @Binds
    @Singleton
    abstract fun bindPopularPhotosRepository(impl: PopularPhotosRepositoryImpl): PopularPhotosRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindFollowUserRepository(impl: FollowUserRepositoryImpl): FollowUserRepository

    @Binds
    @Singleton
    abstract fun bindPhotoDownloadRepository(impl: PhotoDownloadRepositoryImpl): PhotoDownloadRepository
}
