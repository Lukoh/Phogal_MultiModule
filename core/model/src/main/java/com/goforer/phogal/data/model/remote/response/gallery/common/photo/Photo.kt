package com.goforer.phogal.data.model.remote.response.gallery.common.photo

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.goforer.phogal.data.model.remote.response.gallery.common.CurrentUserCollection
import com.goforer.phogal.data.model.remote.response.gallery.common.Urls
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Immutable
@Parcelize
data class Photo(
    val id: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val width: Int,
    val height: Int,
    val color: String?,
    @SerialName("blur_hash") val blurHash: String?,
    val description: String?,
    val user: User,
    @SerialName("current_user_collections") val currentUserCollections: List<CurrentUserCollection>,
    val urls: Urls,
    val links: PhotoLinks,
) : Parcelable {
    companion object {
        fun empty() = Photo(
            id = "",
            createdAt = "",
            updatedAt = "",
            width = 0,
            height = 0,
            color = null,
            blurHash = null,
            description = null,
            user = User.empty(), // 하위 클래스들도 empty() 구현 권장
            currentUserCollections = emptyList(),
            urls = Urls.empty(),
            links = PhotoLinks.empty()
        )
    }
}

