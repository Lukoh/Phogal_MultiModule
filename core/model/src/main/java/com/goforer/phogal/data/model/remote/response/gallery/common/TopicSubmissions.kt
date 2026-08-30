package com.goforer.phogal.data.model.remote.response.gallery.common

import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Serializable
@Immutable
@Parcelize
data class TopicSubmissions(
    val wallpapers: Wallpapers?
) : Parcelable {
    companion object {
        fun empty() = TopicSubmissions(
            wallpapers = null // 또는 필요에 따라 Wallpapers.empty()
        )
    }
}