package com.goforer.phogal.data.datasource.local.room.converter

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import kotlinx.serialization.json.Json

/**
 * Room type converters that (de)serialize the app's rich domain models to JSON columns.
 *
 * Marked with [ProvidedTypeConverter] so the app-wide configured [Json] instance
 * (ignoreUnknownKeys, coerceInputValues, ...) from `NetworkModule.provideJson()` is reused —
 * the exact same serializer that parses the REST API responses. This guarantees that
 * what is stored in Room is byte-for-byte consistent with what the network layer produces,
 * which is a prerequisite for the Single Source of Truth (SSOT) rule.
 */
@ProvidedTypeConverter
class PhogalTypeConverters(private val json: Json) {

    // ─────────────────────────── Photo ───────────────────────────

    @TypeConverter
    fun photoToJson(photo: Photo): String = json.encodeToString(Photo.serializer(), photo)

    @TypeConverter
    fun jsonToPhoto(value: String): Photo = json.decodeFromString(Photo.serializer(), value)

    // ─────────────────────────── Picture ───────────────────────────

    @TypeConverter
    fun pictureToJson(picture: Picture): String = json.encodeToString(Picture.serializer(), picture)

    @TypeConverter
    fun jsonToPicture(value: String): Picture = json.decodeFromString(Picture.serializer(), value)
}
