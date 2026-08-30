package com.goforer.phogal.data.model.local.home.gallery

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class WebViewArgument(
    val firstName: String,
    val url: String
) : Parcelable {
    companion object {
        fun empty() = WebViewArgument(
            firstName = "",
            url = ""
        )
    }
}
