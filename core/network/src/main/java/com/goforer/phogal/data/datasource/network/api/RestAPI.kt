package com.goforer.phogal.data.datasource.network.api

import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.download.TrackDownload
import com.goforer.phogal.data.model.remote.response.gallery.photo.like.LikeResponse
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.data.model.remote.response.gallery.photos.PhotosResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit definition of the Unsplash API surface used by Phogal.
 *
 * Each endpoint is a `suspend fun` returning `Response<T>`. Repositories wrap these calls
 * with [com.goforer.phogal.data.datasource.network.safeApiCall] to convert the raw
 * response (or any thrown exception) into a type-safe
 * [com.goforer.phogal.data.datasource.network.NetworkResult].
 *
 * Authentication:
 * The Unsplash Access Key (client_id) is automatically appended to every request
 * via the `Authorization` header in [com.goforer.phogal.di.module.NetworkModule.provideRequestInterceptor].
 */
interface RestAPI {

    /**
     * Search photos by keyword.
     * [PhotosResponse] contains the list of photos and pagination metadata.
     */
    @GET("search/photos")
    suspend fun getPhotos(
        @Query("query") keyword: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<PhotosResponse>

    /**
     * Retrieve a single photo by its [id].
     */
    @GET("photos/{id}")
    suspend fun getPhoto(
        @Path("id") id: String
    ): Response<Picture>

    /**
     * Retrieve a user's public profile.
     */
    @GET("users/{username}")
    suspend fun getUserPublicProfile(
        @Path("username") username: String
    ): Response<User>

    /**
     * Get a list of photos uploaded by a user.
     */
    @GET("users/{username}/photos")
    suspend fun getUserPhotos(
        @Path("username") username: String,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null
    ): Response<List<Photo>>

    /**
     * Like a photo.
     */
    @POST("photos/{id}/like")
    suspend fun postLike(
        @Path("id") id: String
    ): Response<LikeResponse>

    /**
     * Remove a like from a photo.
     */
    @DELETE("photos/{id}/like")
    suspend fun deleteLike(
        @Path("id") id: String
    ): Response<LikeResponse>

    /**
     * Get the latest/popular photos.
     */
    @GET("photos")
    suspend fun getPopularPhotos(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("order_by") orderBy: String? = null,
    ): Response<List<Photo>>

    /**
     * Tracking a download is required by Unsplash API terms.
     */
    @GET("photos/{id}/download")
    suspend fun trackDownload(
        @Path("id") id: String
    ): Response<TrackDownload>
}
