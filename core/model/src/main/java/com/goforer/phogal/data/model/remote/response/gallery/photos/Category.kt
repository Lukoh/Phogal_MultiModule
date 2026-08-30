package com.goforer.phogal.data.model.remote.response.gallery.photos

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Parcelize
data class Category(
    @SerialName("pretty_slug") val prettySlug: String,
    val slug: String
) : Parcelable {
    companion object {
        fun empty() = Category(
            prettySlug = "",
            slug = ""
        )
    }
}