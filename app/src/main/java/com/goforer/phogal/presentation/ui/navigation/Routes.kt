package com.goforer.phogal.presentation.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

object Routes {

    // ─────────────────────────────── Gallery tab screens ─────────────────────────────────

    @Serializable data object SearchPhotosRoute : NavKey

    @Serializable data object PermissionDialogRoute : NavKey

    @Serializable data class PictureRoute(
        val id: String,
        val showViewPhotosButton: Boolean
    ) : NavKey

    @Serializable data class UserPhotosRoute(
        val name: String,
        val firstName: String,
        val lastName: String,
        val username: String? = null
    ) : NavKey

    @Serializable data class WebViewRoute(
        val firstName: String,
        val url: String
    ) : NavKey

    // ────────────────────────────── Popular photos tab ───────────────────────────────────

    @Serializable data object PopularPhotosRoute : NavKey

    // ────────────────────────────── Notification tab ─────────────────────────────────────

    @Serializable data object NotificationsRoute : NavKey

    @Serializable data class NotificationRoute(val id: String) : NavKey

    // ─────────────────────────────── Setting tab screens ─────────────────────────────────

    @Serializable data object SettingRoute : NavKey
    @Serializable data object BookmarkedPhotosRoute : NavKey
    @Serializable data object FollowingUsersRoute : NavKey
    @Serializable data object NotificationSettingRoute : NavKey
}
