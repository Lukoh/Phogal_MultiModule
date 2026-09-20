package com.goforer.phogal.presentation.ui.navigation.nav3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.DialogSceneStrategy
import com.goforer.base.customtab.openCustomTab
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.presentation.stateholder.business.home.common.photo.info.PictureViewModel
import com.goforer.phogal.presentation.stateholder.business.home.common.user.UserPhotosViewModel
import com.goforer.phogal.presentation.stateholder.business.home.download.PhotoDownloadViewModel
import com.goforer.phogal.presentation.stateholder.business.home.gallery.GalleryViewModel
import com.goforer.phogal.presentation.stateholder.business.home.popularphotos.PopularPhotosViewModel
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.stateholder.business.home.setting.follow.FollowViewModel
import com.goforer.phogal.presentation.stateholder.uistate.UiState
import com.goforer.phogal.presentation.stateholder.uistate.home.bookmark.BookmarkScreenCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.bookmark.rememberBookmarkContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.PictureViewerScreenCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPhotoContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.UserPhotosScreenCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.rememberUserPhotosContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.following.FollowingUserScreenCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.following.rememberFollowingUserContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotosScreenCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchPhotosContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.PopularPhotosScreenCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.rememberPopularPhotosContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.viewer.PictureViewerScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.userphotos.UserPhotosScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.common.webview.WebViewScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.gallery.SearchPhotosScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.notifcation.notifications.NotificationsScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.popularphotos.PopularPhotosScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.setting.SettingScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.setting.bookmark.BookmarkedPhotosScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.setting.following.FollowingUsersScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.setting.notification.NotificationSettingScreen
import com.goforer.phogal.presentation.ui.navigation.Routes

/**
 * Main entry provider for Phogal's Navigation 3 graph.
 *
 * It delegates to tab-specific functions to keep the routing table organized.
 */
fun EntryProviderScope<NavKey>.phogalEntries(navState: NavigationState) {
    galleryTabEntries(navState)
    popularTabEntries(navState)
    notificationTabEntries(navState)
    settingTabEntries(navState)
}

// ─────────────────────────────── Gallery Tab ───────────────────────────────

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun EntryProviderScope<NavKey>.galleryTabEntries(navState: NavigationState) {

    entry<Routes.SearchPhotosRoute>(
        metadata = ListDetailSceneStrategy.listPane(
            detailPlaceholder = { DetailPlaceholder() }
        )
    ) {
        val galleryViewModel: GalleryViewModel = hiltViewModel()
        val followViewModel: FollowViewModel = hiltViewModel()
        val bookmarkViewModel: BookmarkViewModel = hiltViewModel()
        val followingUsers by followViewModel.users.collectAsStateWithLifecycle()
        val bookmarkedPhotos by bookmarkViewModel.photos.collectAsStateWithLifecycle()
        val contentUiState = rememberSearchPhotosContentUiState(galleryViewModel)

        SearchPhotosScreen(
            contentUiState = contentUiState,
            callbacks = SearchPhotosScreenCallbacks(
                isUserFollowed = { id ->
                    followingUsers.any { it.id == id }
                },
                isPhotoBookmarked = { id ->
                    bookmarkedPhotos.any { it.id == id }
                },
                onToggleFollow = { user ->
                    followViewModel.setUserFollow(user)
                },
                onItemClicked = { id ->
                    navState.push(Routes.PictureRoute(id = id, showViewPhotosButton = true))
                },
                onViewPhotos = { name, first, last, user ->
                    navState.push(Routes.UserPhotosRoute(name, first, last, user))
                },
                onOpenWebView = { first, url ->
                    navState.push(Routes.WebViewRoute(first, url))
                }
            )
        )
    }

    entry<Routes.PictureRoute>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { key ->
        val pictureViewModel: PictureViewModel = hiltViewModel()
        val bookmarkViewModel: BookmarkViewModel = hiltViewModel()
        val photoDownloadViewModel: PhotoDownloadViewModel = hiltViewModel()
        val followViewModel: FollowViewModel = hiltViewModel()
        val followingUsers by followViewModel.users.collectAsStateWithLifecycle()
        val bookmarkedPictures by bookmarkViewModel.photos.collectAsStateWithLifecycle()
        val contentUiState = rememberPhotoContentUiState(
            pictureViewModel = pictureViewModel,
            bookmarkViewModel = bookmarkViewModel,
            photoDownloadViewModel = photoDownloadViewModel,
            id = key.id,
            initialVisibleViewButton = key.showViewPhotosButton
        )

        val currentPicture = (contentUiState.pictureState as? UiState.Success)?.data

        contentUiState.enabledBookmark = (bookmarkedPictures.any { it.id == currentPicture?.id })
        PictureViewerScreen(
            contentUiState = contentUiState,
            callbacks = PictureViewerScreenCallbacks(
                isUserFollowed = { user ->
                    followingUsers.any { it.id == user.id }
                },
                onToggleFollow = { user ->
                    followViewModel.setUserFollow(user)
                },
                onViewPhotos = { name, first, last, user ->
                    navState.push(Routes.UserPhotosRoute(name, first, last, user))
                },
                onBackPressed = { navState.pop() },
                onOpenWebView = { first, url ->
                    navState.push(Routes.WebViewRoute(first, url))
                }
            )
        )
    }

    entry<Routes.UserPhotosRoute> { key ->
        val userPhotosViewModel: UserPhotosViewModel = hiltViewModel()
        val followViewModel: FollowViewModel = hiltViewModel()
        val bookmarkViewModel: BookmarkViewModel = hiltViewModel()
        val followingUsers by followViewModel.users.collectAsStateWithLifecycle()
        val bookmarkedPhotos by bookmarkViewModel.photos.collectAsStateWithLifecycle()
        val contentUiState = rememberUserPhotosContentUiState(
            baseUiState = rememberBaseUiState(),
            userPhotosViewModel = userPhotosViewModel,
            name = key.name,
            firstName = key.firstName
        )

        UserPhotosScreen(
            contentUiState = contentUiState,
            callbacks = UserPhotosScreenCallbacks(
                isUserFollowed = { id ->
                    followingUsers.any { it.id == id }
                },
                isPhotoBookmarked = { id ->
                    bookmarkedPhotos.any { it.id == id }
                },
                onToggleFollow = { user ->
                    followViewModel.setUserFollow(user)
                },
                onItemClicked = { id ->
                    navState.push(Routes.PictureRoute(id = id, showViewPhotosButton = false))
                },
                onOpenWebView = { first, url ->
                    navState.push(Routes.WebViewRoute(first, url))
                },
                onBackPressed = { navState.pop() }
            )
        )
    }

    entry<Routes.WebViewRoute> { key ->
        WebViewScreen(
            firstName = key.firstName,
            url = key.url,
            onBackPressed = { navState.pop() }
        )
    }

    entry<Routes.PermissionDialogRoute>(
        metadata = DialogSceneStrategy.dialog()
    ) {
        PermissionDialogContent(
            onDismiss = { navState.pop() },
            onConfirm = { navState.pop() }
        )
    }
}

// ─────────────────────────── Popular Photos Tab ───────────────────────────

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun EntryProviderScope<NavKey>.popularTabEntries(navState: NavigationState) {
    entry<Routes.PopularPhotosRoute>(
        metadata = ListDetailSceneStrategy.listPane(
            detailPlaceholder = { DetailPlaceholder() }
        )
    ) {
        val popularPhotosViewModel: PopularPhotosViewModel = hiltViewModel()
        val followViewModel: FollowViewModel = hiltViewModel()
        val bookmarkViewModel: BookmarkViewModel = hiltViewModel()
        val followingUsers by followViewModel.users.collectAsStateWithLifecycle()
        val bookmarkedPhotos by bookmarkViewModel.photos.collectAsStateWithLifecycle()
        val contentUiState = rememberPopularPhotosContentUiState(popularPhotosViewModel)

        PopularPhotosScreen(
            contentUiState = contentUiState,
            callbacks = PopularPhotosScreenCallbacks(
                isUserFollowed = { id ->
                    followingUsers.any { it.id == id }
                },
                isPhotoBookmarked = { id ->
                    bookmarkedPhotos.any { it.id == id }
                },
                onToggleFollow = { user ->
                    followViewModel.setUserFollow(user)
                },
                onItemClicked = { id, _ ->
                    navState.push(Routes.PictureRoute(id = id, showViewPhotosButton = true))
                },
                onViewPhotos = { name, first, last, user ->
                    navState.push(Routes.UserPhotosRoute(name, first, last, user))
                },
                onOpenWebView = { first, url ->
                    navState.push(Routes.WebViewRoute(first, url))
                }
            )
        )
    }
}

// ────────────────────────────── Notification Tab ───────────────────────────────

private fun EntryProviderScope<NavKey>.notificationTabEntries(navState: NavigationState) {
    entry<Routes.NotificationsRoute> {
        NotificationsScreen(
            onItemClicked = { id -> navState.push(Routes.NotificationRoute(id)) }
        )
    }

    entry<Routes.NotificationRoute> {
        NotificationsScreen(
            onItemClicked = { navState.push(Routes.NotificationRoute(it)) }
        )
    }
}

// ─────────────────────────────── Setting Tab ───────────────────────────────

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun EntryProviderScope<NavKey>.settingTabEntries(navState: NavigationState) {
    entry<Routes.SettingRoute> {
        SettingScreen(
            baseUiState = rememberBaseUiState(),
            onItemClicked = { context, index ->
                when (index) {
                    0 -> navState.push(Routes.BookmarkedPhotosRoute)
                    1 -> navState.push(Routes.FollowingUsersRoute)
                    2 -> navState.push(Routes.NotificationSettingRoute)
                    4 -> openCustomTab(context, "https://lukoh.github.io/Phogal/")
                    7 -> openCustomTab(context, "https://github.com/Lukoh/Phogal")
                    else -> Unit
                }
            }
        )
    }

    entry<Routes.BookmarkedPhotosRoute>(
        metadata = ListDetailSceneStrategy.listPane(
            detailPlaceholder = { DetailPlaceholder() }
        )
    ) {
        val bookmarkViewModel: BookmarkViewModel = hiltViewModel()
        val followViewModel: FollowViewModel = hiltViewModel()
        val followingUsers by followViewModel.users.collectAsStateWithLifecycle()
        val contentUiState = rememberBookmarkContentUiState(
            bookmarkViewModel = bookmarkViewModel,
            initialEnabledLoadPhotos = true
        )

        BookmarkedPhotosScreen(
            contentUiState = contentUiState,
            callbacks = BookmarkScreenCallbacks(
                onItemClicked = { picture, _ ->
                    navState.push(Routes.PictureRoute(id = picture.id, showViewPhotosButton = false))
                },
                onBackPressed = { navState.pop() },
                onViewPhotos = { name, first, last, user ->
                    navState.push(Routes.UserPhotosRoute(name, first, last, user))
                },
                onOpenWebView = { first, url ->
                    navState.push(Routes.WebViewRoute(first, url))
                },
                isUserFollowed = { user ->
                    followingUsers.any { it.id == user.id }
                },
                onToggleFollow = { user ->
                    followViewModel.setUserFollow(user)
                }
            )
        )
    }

    entry<Routes.FollowingUsersRoute>(
        metadata = ListDetailSceneStrategy.listPane(
            detailPlaceholder = { DetailPlaceholder() }
        )
    ) {
        val followViewModel: FollowViewModel = hiltViewModel()
        val contentUiState = rememberFollowingUserContentUiState(
            followViewModel = followViewModel,
            initialEnabledLoadPhotos = true
        )

        FollowingUsersScreen(
            contentUiState = contentUiState,
            callbacks = FollowingUserScreenCallbacks(
                onBackPressed = { navState.pop() },
                onViewPhotos = { name, first, last, user ->
                    navState.push(Routes.UserPhotosRoute(name, first, last, user))
                },
                onOpenWebView = { first, url ->
                    navState.push(Routes.WebViewRoute(first, url))
                }
            )
        )
    }

    entry<Routes.NotificationSettingRoute> {
        NotificationSettingScreen(onBackPressed = { navState.pop() })
    }
}

// ─────────────────────────────── Helpers ───────────────────────────────

@Composable
private fun DetailPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.bottom_navigation_photo),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A dialog-themed surface for the permission entry.
 *
 * We avoid using `AlertDialog` here because [DialogSceneStrategy.dialog]
 * already provides the standard [androidx.compose.ui.window.Dialog] window.
 * Using `Surface` with standard padding and shape ensures a clean Material 3
 * look without double-wrapping the dialog window.
 */
@Composable
private fun PermissionDialogContent(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 6.dp,
        modifier = Modifier.padding(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(id = R.string.bottom_navigation_photo),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.bottom_navigation_photo),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                TextButton(onClick = onConfirm) {
                    Text("OK")
                }
            }
        }
    }
}
