package com.goforer.phogal.data.repository.download

import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.datasource.network.api.RestAPI
import com.goforer.phogal.data.datasource.network.safeApiCall
import com.goforer.phogal.data.model.remote.response.gallery.photo.download.TrackDownload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoDownloadRepositoryImpl @Inject constructor(
    private val api: RestAPI
) : PhotoDownloadRepository {
    override suspend fun getFinalDownloadUrl(id: String): NetworkResult<TrackDownload> = safeApiCall {
        api.trackDownload(id)
    }
}