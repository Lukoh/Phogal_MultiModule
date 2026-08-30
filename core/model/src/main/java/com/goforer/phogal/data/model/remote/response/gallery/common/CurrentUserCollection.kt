package com.goforer.phogal.data.model.remote.response.gallery.common

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Immutable
@Parcelize
data class CurrentUserCollection(
    val id: Int,
    val title: String,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("last_collected_at") val lastCollectedAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("cover_photo") val coverPhoto: CoverPhoto?,
    val user: User?
): Parcelable {
    companion object {
        fun empty() = CurrentUserCollection(
            id = 0,
            title = "",
            publishedAt = "",
            lastCollectedAt = "",
            updatedAt = "",
            coverPhoto = null,
            user = null
        )
    }
}