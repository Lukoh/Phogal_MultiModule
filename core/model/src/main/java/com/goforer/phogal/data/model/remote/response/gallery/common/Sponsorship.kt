package com.goforer.phogal.data.model.remote.response.gallery.common

import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName

@Serializable
@Parcelize
@Immutable
data class Sponsorship(
    @SerialName("impression_urls") val impressionUrls: List<String>?,
    val sponsor: Sponsor,
    val tagline: String,
    @SerialName("tagline_url") val taglineUrl: String?
) : Parcelable {
    companion object {
        fun empty() = Sponsorship(
            impressionUrls = emptyList(),
            sponsor = Sponsor.empty(), // 하위 객체도 empty() 호출
            tagline = "",
            taglineUrl = null
        )
    }
}