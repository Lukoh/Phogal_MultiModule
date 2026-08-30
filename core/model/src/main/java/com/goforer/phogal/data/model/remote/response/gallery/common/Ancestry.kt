package com.goforer.phogal.data.model.remote.response.gallery.common

import kotlinx.serialization.Serializable
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.goforer.phogal.data.model.remote.response.gallery.photos.Category
import com.goforer.phogal.data.model.remote.response.gallery.photos.Subcategory
import com.goforer.phogal.data.model.remote.response.gallery.photos.Type
import kotlinx.parcelize.Parcelize

@Serializable
@Immutable
@Parcelize
data class Ancestry(
    val category: Category?,
    val subcategory: Subcategory?,
    val type: Type?
) : Parcelable {
    companion object {
        fun empty() = Ancestry(
            category = null,
            subcategory = null,
            type = null
        )
    }
}