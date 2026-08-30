package com.goforer.phogal.data.model.remote.response.gallery.common

import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Immutable
@Parcelize
data class Source(
    val ancestry: Ancestry?,
    @SerialName("cover_photo") val coverPhoto: CoverPhoto?,
    val description: String? = null,
    @SerialName("meta_description") val metaDescription: String?,
    @SerialName("meta_title") val metaTitle: String?,
    val subtitle: String?,
    val title: String?
) : Parcelable {
    companion object {
        fun empty() = Source(
            ancestry = null,
            coverPhoto = null,
            description = null,
            metaDescription = null,
            metaTitle = null,
            subtitle = null,
            title = null
        )
    }
}