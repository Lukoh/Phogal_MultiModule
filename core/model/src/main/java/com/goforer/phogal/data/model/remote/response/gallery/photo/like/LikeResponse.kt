package com.goforer.phogal.data.model.remote.response.gallery.photo.like

import kotlinx.serialization.Serializable
import android.os.Parcelable
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class LikeResponse(
    val photo: Photo,
    val user: User
) : Parcelable {
    companion object {
        fun empty() = LikeResponse(
            photo = Photo.empty(), // 하위 객체들의 empty()를 연쇄 호출
            user = User.empty()
        )
    }
}