package com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo

import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
@Immutable
data class RelatedCollections(
    val result: List<Result>,
    val total: Long,
    val type: String
) : Parcelable {
    companion object {
        val EMPTY = RelatedCollections(
            result = emptyList(),
            total = 0L,
            type = ""
        )
    }
}