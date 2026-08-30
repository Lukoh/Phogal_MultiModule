package com.goforer.phogal.data.model.local.home.gallery

import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Serializable
@Immutable
@Parcelize
data class PictureArgument(
    val id: String,
    val visibleViewPhotosButton: Boolean
) : Parcelable {
    companion object {
        fun empty() = PictureArgument(
            id = "",
            visibleViewPhotosButton = false
        )
    }
}
