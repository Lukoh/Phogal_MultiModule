package com.goforer.phogal.data.model.remote.response.gallery.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Social(
    @SerialName("instagram_username") val instagramUsername: String?,
    @SerialName("paypal_email") val paypalEmail: String?,
    @SerialName("portfolio_url") val portfolioUrl: String?,
    @SerialName("twitter_username")val twitterUsername: String?
) : Parcelable {
    companion object {
        fun empty() = Social(
            instagramUsername = null,
            paypalEmail = null,
            portfolioUrl = null,
            twitterUsername = null
        )
    }
}