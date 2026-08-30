package com.goforer.phogal.data.model.remote.response.gallery.photo.download

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class TrackDownload(
    val url: String
) : Parcelable {
    companion object {
        fun empty() = TrackDownload(
            url = ""
        )
    }
}
