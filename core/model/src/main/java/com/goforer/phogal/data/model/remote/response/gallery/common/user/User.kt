package com.goforer.phogal.data.model.remote.response.gallery.common.user

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.goforer.phogal.data.model.remote.response.gallery.common.ProfileImage
import com.goforer.phogal.data.model.remote.response.gallery.common.Social
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
@Immutable
@Parcelize
data class User(
    @SerialName("accepted_tos") val acceptedTos: Boolean,
    val bio: String? = null,
    @SerialName("first_name") val firstName: String,
    @SerialName("for_hire") val forHire: Boolean,
    val id: String,
    @SerialName("instagram_username") val instagramUsername: String?,
    @SerialName("last_name") val lastName: String?,
    val links: UserLinks,
    val location: String?,
    val name: String,
    @SerialName("portfolio_url") val portfolioUrl: String?,
    @SerialName("profile_image") val profileImage: ProfileImage,
    val social: Social,
    @SerialName("total_collections") val totalCollections: Int,
    @SerialName("total_likes") val totalLikes: Int,
    @SerialName("total_photos") val totalPhotos: Int,
    @SerialName("twitter_username") val twitterUsername: String?,
    @SerialName("updated_at") val updatedAt: String,
    val username: String?
) : Parcelable {
    /**
     * Renders this `User` as JSON for diagnostic purposes (logs, debug toasts).
     *
     * Uses a self-contained `Json` instance rather than going through the DI
     * graph — `toString()` should never be a dependency on Hilt, and the
     * default formatting is good enough for log output. If you need the
     * project's configured `Json` (e.g. for production data flow), inject it
     * from `NetworkModule.provideJson()` and call `Json.encodeToString(user)`
     * yourself instead of relying on `toString()`.
     */
    override fun toString(): String = Json.encodeToString(serializer(), this)

    companion object {
        fun empty() = User(
            acceptedTos = false,
            bio = null,
            firstName = "",
            forHire = false,
            id = "",
            instagramUsername = null,
            lastName = null,
            links = UserLinks.empty(), // 하위 클래스도 empty() 구현 권장
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
    }
}