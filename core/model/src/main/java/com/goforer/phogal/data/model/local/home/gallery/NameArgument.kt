package com.goforer.phogal.data.model.local.home.gallery

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class NameArgument(
    val name: String,
    val firstName: String,
    val lastName: String,
    val username: String?
) : Parcelable {
    companion object {
        fun empty() = NameArgument(
            name = "",
            firstName = "",
            lastName = "",
            username = null
        )
    }
}
