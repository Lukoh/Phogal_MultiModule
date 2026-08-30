package com.goforer.phogal.data.model.remote.response.gallery.common.photo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class PhotoLinks(
    val self: String,
    val html: String,
    val download: String,
    @SerialName("download_location") val downloadLocation: String
) : Parcelable {
    companion object {
        fun empty() = PhotoLinks(
            self = "",
            html = "",
            download = "",
            downloadLocation = ""
        )
    }
}