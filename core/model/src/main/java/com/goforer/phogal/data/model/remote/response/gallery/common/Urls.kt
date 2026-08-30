package com.goforer.phogal.data.model.remote.response.gallery.common

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class Urls(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String
) : Parcelable {
    companion object {
        fun empty() = Urls(
            raw = "",
            full = "",
            regular = "",
            small = "",
            thumb = ""
        )
    }
}