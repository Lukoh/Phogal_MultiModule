package com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class TagsPreview(
    val title: String,
    val type: String
) : Parcelable {
    companion object {
        fun empty() = TagsPreview(
            title = "",
            type = ""
        )
    }
}