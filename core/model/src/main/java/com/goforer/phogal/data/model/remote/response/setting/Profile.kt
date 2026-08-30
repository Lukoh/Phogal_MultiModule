package com.goforer.phogal.data.model.remote.response.setting

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class Profile(
    val id: Int,
    val name: String,
    val sex: String,
    var favor: Boolean,
    var followed : Boolean = false,
    val email: String,
    val profileImage: String,
    val personality: String,
    val cellphone: String,
    val address: String,
    val birthday: String,
    val reputation: String,
    var deleted: Boolean
) : Parcelable {
    companion object {
        val EMPTY = Profile(
            id = -1,
            name = "",
            sex = "",
            favor = false,
            followed = false,
            email = "",
            profileImage = "",
            personality = "",
            cellphone = "",
            address = "",
            birthday = "",
            reputation = "",
            deleted = false
        )
    }
}
