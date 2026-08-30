package com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo

import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.goforer.phogal.data.model.remote.response.gallery.common.Urls
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Immutable
@Parcelize
data class PreviewPhoto(
    @SerialName("blur_hash") val blurHash: String,
    @SerialName("created_at") val createdAt: String,
    val id: String,
    val slug: String,
    @SerialName("updated_at") val updatedAt: String,
    val urls: Urls
) : Parcelable {
    companion object {
        fun empty() = PreviewPhoto(
            blurHash = "",
            createdAt = "",
            id = "",
            slug = "",
            updatedAt = "",
            urls = Urls.empty() // 하위 Urls 클래스 초기화 호출
        )
    }
}