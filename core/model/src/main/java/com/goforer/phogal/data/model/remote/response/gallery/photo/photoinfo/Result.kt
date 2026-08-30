package com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo

import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.goforer.phogal.data.model.remote.response.gallery.common.CoverPhoto
import com.goforer.phogal.data.model.remote.response.gallery.common.Links
import com.goforer.phogal.data.model.remote.response.gallery.common.ProfileImage
import com.goforer.phogal.data.model.remote.response.gallery.common.Social
import com.goforer.phogal.data.model.remote.response.gallery.common.Tag
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.common.user.UserLinks
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Parcelize
@Immutable
data class Result(
    @SerialName("cover_photo") val coverPhoto: CoverPhoto?,
    val curated: Boolean,
    val description: String?,
    val featured: Boolean,
    val id: String,
    @SerialName("last_collected_at") val lastCollectedAt: String,
    val links: Links,
    @SerialName("preview_photos") val previewPhotos: List<PreviewPhoto>,
    val private: Boolean,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("share_key") val shareKey: String,
    val tags: List<Tag>,
    val title: String,
    @SerialName("total_photos") val totalPhotos: Long,
    @SerialName("updated_at")val updatedAt: String,
    val user: User
) : Parcelable {
    companion object {
        val EMPTY = Result(
            coverPhoto = null,
            curated = false,
            description = "",
            featured = false,
            id = "",
            lastCollectedAt = "",
            links = Links(
                self = "",
                html = "",
                download = "",
                downloadLocation = ""
            ), // Links 클래스에도 EMPTY 정의 권장
            previewPhotos = persistentListOf(),
            private = false,
            publishedAt = "",
            shareKey = "",
            tags = persistentListOf(),
            title = "",
            totalPhotos = 0L,
            updatedAt = "",
            user = User(
                acceptedTos = false,
                bio = null,
                firstName = "",
                forHire = false,
                id = "",
                instagramUsername = null,
                lastName = null,
                links = UserLinks.empty(),
                location = null,
                name = "",
                portfolioUrl = null,
                profileImage = ProfileImage.empty(),
                social = Social.empty(),
                totalCollections = 0,
                totalLikes = 0,
                totalPhotos = 0,
                twitterUsername = null,
                updatedAt = "",
                username = ""
            )
        )
    }
}