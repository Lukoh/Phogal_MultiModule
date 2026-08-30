package com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo

import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Serializable
@Immutable
@Parcelize
data class Location(
    val city: String?,
    val country: String?,
    val name: String?,
    val position: Position
) : Parcelable {
    companion object {
        fun empty() = Location(
            city = null,
            country = null,
            name = null,
            position = Position.empty() // Position 클래스에도 empty() 구현 권장
        )
    }
}