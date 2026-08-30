package com.goforer.phogal.data.model.remote.response.gallery.photo.download

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.File

object FileSerializer : KSerializer<File> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("File", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: File) {
        encoder.encodeString(value.absolutePath)
    }

    override fun deserialize(decoder: Decoder): File {
        return File(decoder.decodeString())
    }
}

@Serializable
@Immutable
@Parcelize
data class PhotoDownload(
    val isDownloading: Boolean,
    val progress: Float,
    val savedFile: @Serializable(with = FileSerializer::class) File?,
    val errorMessage: String?
) : Parcelable {
    companion object {
        fun empty() = PhotoDownload(
            isDownloading = false,
            progress = 0F,
            savedFile = null,
            errorMessage = null
        )
    }
}
