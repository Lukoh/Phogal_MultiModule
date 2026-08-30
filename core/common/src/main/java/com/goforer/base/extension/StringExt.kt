package com.goforer.base.extension

import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.common.user.UserLinks
import kotlinx.serialization.json.Json

/**
 * Internal singleton Json instance for the lightweight extensions in this file.
 *
 * Why a private top-level value rather than DI:
 *  - `String.toUser()` is an extension function and cannot receive a `Json`
 *    parameter without changing every call site.
 *  - The configuration mirrors `NetworkModule.provideJson()` — the rare drift
 *    between them would be a bug, but is also a small surface area to audit.
 *
 * If a future refactor needs a single source of truth for `Json`, replace
 * call sites of `String.toUser()` with `sharedJson.decodeFromString<User>(s)`
 * where `sharedJson` is the injected instance.
 */
private val sharedJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
    explicitNulls = false
}

/**
 * Decodes this JSON string as a [User].
 *
 * Migrated from `Gson().fromJson(this, User::class.java)`. Behavior:
 *  - Throws if the JSON shape doesn't match `User` (same as before).
 *  - With `ignoreUnknownKeys = true`, tolerates extra fields that Unsplash
 *    might add later — the previous Gson version was equally tolerant.
 */
fun String.toUser(): User = sharedJson.decodeFromString<User>(this)
fun String.toUserLinks() : UserLinks = sharedJson.decodeFromString<UserLinks>(this)
fun String.toPhoto(): Photo = sharedJson.decodeFromString<Photo>(this)
