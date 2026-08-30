package com.goforer.phogal.data.model.remote.response.gallery.common

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class ProfileImage(
    val large: String,
    val medium: String,
    val small: String
) : Parcelable {
    companion object {
        fun empty() = ProfileImage(
            large = "",
            medium = "",
            small = ""
        )
    }
}