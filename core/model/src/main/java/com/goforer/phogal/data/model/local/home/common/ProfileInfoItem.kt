package com.goforer.phogal.data.model.local.home.common

import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class ProfileInfoItem(
    val text: String,
    @DrawableRes
    val iconResId: Int,
    val position: Int
) : Parcelable {
    companion object {
        fun empty() = ProfileInfoItem(
            text = "",
            iconResId = 0,
            position = 0
        )
    }
}