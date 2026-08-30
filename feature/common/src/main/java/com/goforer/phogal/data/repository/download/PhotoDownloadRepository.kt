package com.goforer.phogal.data.repository.download

import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.model.remote.response.gallery.photo.download.TrackDownload

interface PhotoDownloadRepository {
    suspend fun getFinalDownloadUrl(id: String): NetworkResult<TrackDownload>
}