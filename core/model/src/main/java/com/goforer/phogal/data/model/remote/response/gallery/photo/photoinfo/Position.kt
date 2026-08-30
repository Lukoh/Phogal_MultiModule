package com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo

import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Serializable
@Immutable
@Parcelize
data class Position(
    val latitude: Double?,
    val longitude: Double?
) : Parcelable {
    companion object {
        fun empty() = Position(
            latitude = 0.0,
            longitude = 0.0
        )
    }
}