package com.goforer.phogal.data.datasource.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "phogal_preferences")

/**
 * Local data source powered by Jetpack DataStore (Preferences).
 *
 * Handles persistence of bookmarks, search history, following status, and
 * user settings. Complex objects are serialized to JSON via kotlinx.serialization.
 */
@Singleton
class LocalDataSource @Inject constructor(
    private val context: Context,
    private val json: Json,
    private val cookieJar: PersistentCookieJar? = null
) {
    private object PreferencesKeys {
        val BOOKMARK_PHOTOS = stringPreferencesKey("key_bookmark_photos")
        val SEARCH_WORDS = stringPreferencesKey("search_word_list")
        val FOLLOWING_USER = stringPreferencesKey("key_following_user")
        val NOTIF_FOLLOWING = booleanPreferencesKey("key_notification_following_enabled")
        val NOTIF_LATEST = booleanPreferencesKey("key_notification_latest_enabled")
        val NOTIF_COMMUNITY = booleanPreferencesKey("key_notification_community_enabled")
    }

    private val pictureListSerializer = ListSerializer(Picture.serializer())
    private val userListSerializer = ListSerializer(User.serializer())
    private val stringListSerializer = ListSerializer(String.serializer())

    /**
     * Resets all local data including preferences, cache, and cookies.
     */
    suspend fun logOut() {
        Timber.i("Logging out - clearing all local data")
        clearPreferences()
        deleteCache()
        cookieJar?.clear()
    }

    private suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
    }

    private fun deleteCache() {
        runCatching {
            deleteDir(context.cacheDir)
        }.onFailure { e -> Timber.e(e, "Failed to delete cache directory") }
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list() ?: return false
            for (child in children) {
                if (!deleteDir(File(dir, child))) return false
            }
            dir.delete()
        } else {
            dir?.delete() ?: false
        }
    }

    // ─────────────────────────── Bookmarks ───────────────────────────

    val bookmarkedPhotosFlow: Flow<List<Picture>> = observeList(
        PreferencesKeys.BOOKMARK_PHOTOS,
        pictureListSerializer
    )

    fun isPhotoBookmarkedFlow(id: String): Flow<Boolean> = bookmarkedPhotosFlow
        .map { photos -> photos.any { it.id == id } }
        .distinctUntilChanged()

    fun isPhotoBookmarkedFlow(photo: Picture): Flow<Boolean> = bookmarkedPhotosFlow
        .map { photos ->
            photos.any { it.id == photo.id || it.urls.raw == photo.urls.raw }
        }
        .distinctUntilChanged()

    suspend fun toggleBookmarkPhoto(photo: Picture) {
        toggleInList(PreferencesKeys.BOOKMARK_PHOTOS, pictureListSerializer, photo) { it.id == photo.id }
    }

    // ─────────────────────────── Search History ───────────────────────────

    val searchWordsFlow: Flow<List<String>> = observeList(
        PreferencesKeys.SEARCH_WORDS,
        stringListSerializer
    )

    suspend fun setSearchWords(words: List<String>) {
        updateValue(PreferencesKeys.SEARCH_WORDS, json.encodeToString(stringListSerializer, words))
    }

    // ─────────────────────────── Following ───────────────────────────

    val followingUsersFlow: Flow<List<User>> = observeList(
        PreferencesKeys.FOLLOWING_USER,
        userListSerializer
    )

    fun isUserFollowedFlow(user: User): Flow<Boolean> = followingUsersFlow
        .map { users -> users.any { it.id == user.id || it.username == user.username } }
        .distinctUntilChanged()

    suspend fun toggleFollowingUser(user: User) {
        toggleInList(PreferencesKeys.FOLLOWING_USER, userListSerializer, user) { it.id == user.id }
    }

    // ─────────────────────────── Notifications ───────────────────────────

    val enabledFollowingNotificationFlow: Flow<Boolean> = observeBoolean(PreferencesKeys.NOTIF_FOLLOWING, true)
    val enabledLatestNotificationFlow: Flow<Boolean> = observeBoolean(PreferencesKeys.NOTIF_LATEST, true)
    val enabledCommunityNotificationFlow: Flow<Boolean> = observeBoolean(PreferencesKeys.NOTIF_COMMUNITY, true)

    suspend fun setFollowingNotificationEnabled(enabled: Boolean) = updateValue(PreferencesKeys.NOTIF_FOLLOWING, enabled)
    suspend fun setLatestNotificationEnabled(enabled: Boolean) = updateValue(PreferencesKeys.NOTIF_LATEST, enabled)
    suspend fun setCommunityNotificationEnabled(enabled: Boolean) = updateValue(PreferencesKeys.NOTIF_COMMUNITY, enabled)

    // ─────────────────────────── Generic Helpers ───────────────────────────

    private fun <T> observeList(
        key: Preferences.Key<String>,
        serializer: KSerializer<List<T>>
    ): Flow<List<T>> = context.dataStore.data
        .handleErrors()
        .map { preferences ->
            val jsonStr = preferences[key]
            if (jsonStr.isNullOrEmpty()) {
                emptyList()
            } else {
                runCatching { json.decodeFromString(serializer, jsonStr) }
                    .getOrElse {
                        Timber.w(it, "Failed to parse stored list for key: ${key.name}")
                        emptyList()
                    }
            }
        }
        .distinctUntilChanged()

    private fun observeBoolean(
        key: Preferences.Key<Boolean>,
        defaultValue: Boolean
    ): Flow<Boolean> = context.dataStore.data
        .handleErrors()
        .map { it[key] ?: defaultValue }
        .distinctUntilChanged()

    private suspend fun <T> toggleInList(
        key: Preferences.Key<String>,
        serializer: KSerializer<List<T>>,
        item: T,
        predicate: (T) -> Boolean
    ) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[key]
            val list = if (currentJson.isNullOrEmpty()) {
                mutableListOf()
            } else {
                runCatching { json.decodeFromString(serializer, currentJson).toMutableList() }
                    .getOrDefault(mutableListOf())
            }

            val existingIndex = list.indexOfFirst(predicate)
            if (existingIndex == -1) {
                list.add(item)
            } else {
                list.removeAt(existingIndex)
            }

            preferences[key] = json.encodeToString(serializer, list)
        }
    }

    private suspend fun <T> updateValue(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    private fun Flow<Preferences>.handleErrors(): Flow<Preferences> = catch { exception ->
        if (exception is IOException) {
            Timber.e(exception, "Error reading DataStore preferences.")
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }
}
