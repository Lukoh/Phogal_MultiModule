package com.goforer.phogal.data.model.local.home.setting

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class SettingItem(
    @StringRes val textRes: Int,
    @DrawableRes val drawable: Int
) : Parcelable {
    companion object {
        fun empty() = SettingItem(
            textRes = 0,
            drawable = 0
        )
    }
}
