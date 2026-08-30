package com.goforer.phogal.data.model.remote.response.gallery.common.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class UserLinks(
    val self: String,
    val html: String,
    val photos: String
) : Parcelable {
    companion object {
        fun empty() = UserLinks(
            self = "",
            html = "",
            photos = ""
        )
    }
}