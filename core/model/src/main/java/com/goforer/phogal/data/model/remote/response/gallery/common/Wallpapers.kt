package com.goforer.phogal.data.model.remote.response.gallery.common

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Parcelize
data class Wallpapers(
    @SerialName("approved_on") val approvedOn: String?,
    val status: String?
) : Parcelable {
    companion object {
        fun empty() = Wallpapers(
            approvedOn = null,
            status = null
        )
    }
}