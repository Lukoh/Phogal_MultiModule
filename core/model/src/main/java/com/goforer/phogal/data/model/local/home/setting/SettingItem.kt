package com.goforer.phogal.data.model.local.home.setting

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class SettingItem(
    val text: String,
    val drawable: Int
) : Parcelable {
    companion object {
        fun empty() = SettingItem(
            text = "",
            drawable = 0
        )
    }
}
