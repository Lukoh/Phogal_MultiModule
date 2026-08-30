package com.goforer.phogal.data.model.local.error

import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Serializable
@Immutable
@Parcelize
data class Errors(
    val errors: List<String>
) : Parcelable {
    companion object {
        fun empty() = Errors(
            errors = emptyList()
        )
    }
}