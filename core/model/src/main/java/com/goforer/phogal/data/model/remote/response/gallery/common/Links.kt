package com.goforer.phogal.data.model.remote.response.gallery.common

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Parcelize
data class Links(
    val self: String,
    val html: String,
    val download: String,
    @SerialName("download_location") val downloadLocation: String
) : Parcelable {
    companion object {
        fun empty() = Links(
            self = "",
            html = "",
            download = "",
            downloadLocation = ""
        )
    }
}