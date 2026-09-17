package com.goforer.phogal.presentation.stateholder.uistate.home.common.photo

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goforer.base.utils.download.PhotoAlreadyExistsException
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.download.TrackDownload
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.business.home.common.photo.info.PictureViewModel
import com.goforer.phogal.presentation.stateholder.business.home.download.PhotoDownloadViewModel
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.UiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.base.BasePhotoContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.viewer.DownloadDialogState
import kotlinx.coroutines.launch

@Stable
data class UserContainerCallbacks(
    val onFollowClick: (User) -> Unit,
    val onShowUserInfo: (User) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit
)

@Stable
data class PictureViewerCallbacks(
    val userContainerCallbacks: UserContainerCallbacks,
    val onShownPhoto: (picture: Picture) -> Unit,
    val onSuccess: (isSuccessful: Boolean) -> Unit,
    val onDownloadTriggered: (url: String) -> Unit,
    val onDownloadPhoto: (url: String) -> Unit,
    val onRetry: () -> Unit,
    val onDismissPopup: () -> Unit,
    val onDismissDialog: () -> Unit
)

@Stable
data class PictureViewerScreenCallbacks(
    val isUserFollowed: (User) -> Boolean,
    val onToggleFollow: (User) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onBackPressed: () -> Unit,
    val onOpenWebView: (firstName: String, url: String) -> Unit,
    val onStart: () -> Unit = {},
    val onStop: () -> Unit = {}
)

@Stable
class PhotoContentUiState internal constructor(
    val pictureViewModel: PictureViewModel,
    val bookmarkViewModel: BookmarkViewModel,
    val photoDownloadViewModel: PhotoDownloadViewModel,
    val pictureState: UiState<Picture>,
    val trackDownloadState: UiState<TrackDownload>,
    val id: String,
    override val baseUiState: BaseUiState,
    initialShowPopup: Boolean,
    initialDialogState: DownloadDialogState,
    initialVisibleViewButton: Boolean,
    initialEnabledBookmark: Boolean,
    initialVisible: Boolean,
    initialSelectedUser: User?,
) : BasePhotoContentUiState(
    baseUiState = baseUiState,
    initialVisible = initialVisible,
    initialError = null,
    initialSelectedUser = initialSelectedUser,
) {
    var showPopup: Boolean by mutableStateOf(initialShowPopup)
    var dialogState: DownloadDialogState by mutableStateOf(initialDialogState)
    var visibleViewButton: Boolean by mutableStateOf(initialVisibleViewButton)
    var enabledBookmark: Boolean by mutableStateOf(initialEnabledBookmark)
}

@Composable
fun rememberPhotoContentUiState(
    pictureViewModel: PictureViewModel,
    bookmarkViewModel: BookmarkViewModel,
    photoDownloadViewModel: PhotoDownloadViewModel,
    id: String = "",
    baseUiState: BaseUiState = rememberBaseUiState(),
    initialShowPopup: Boolean = false,
    initialDialogState: DownloadDialogState = DownloadDialogState.Idle,
    initialVisibleViewButton: Boolean = false,
    initialEnabledBookmark: Boolean = false,
    initialVisible: Boolean = false,
    initialSelectedUser: User? = null
): PhotoContentUiState {
    val pictureState by pictureViewModel.picture.collectAsStateWithLifecycle()
    val trackDownloadState by photoDownloadViewModel.trackDownload.collectAsStateWithLifecycle()

    return remember(
        baseUiState,
        pictureViewModel,
        bookmarkViewModel,
        photoDownloadViewModel,
        pictureState,
        trackDownloadState
    ) {
        PhotoContentUiState(
            pictureViewModel = pictureViewModel,
            bookmarkViewModel = bookmarkViewModel,
            photoDownloadViewModel = photoDownloadViewModel,
            pictureState = pictureState,
            trackDownloadState = trackDownloadState,
            id = id,
            baseUiState = baseUiState,
            initialShowPopup = initialShowPopup,
            initialDialogState = initialDialogState,
            initialVisibleViewButton = initialVisibleViewButton,
            initialEnabledBookmark = initialEnabledBookmark,
            initialVisible = initialVisible,
            initialSelectedUser = initialSelectedUser
        )
    }
}

/**
 * A helper structure to hold internal UI logic and stable callbacks.
 */
@Stable
class PictureViewerInternalCallbacks internal constructor(
    val viewerCallbacks: PictureViewerCallbacks,
    val onLikedClick: () -> Unit,
    val onBookmarkClick: (Picture) -> Unit,
    val onBackPressed: () -> Unit
)

@Composable
fun rememberPictureViewerInternalCallbacks(
    contentUiState: PhotoContentUiState,
    screenCallbacks: PictureViewerScreenCallbacks,
    snackbarHostState: SnackbarHostState
): PictureViewerInternalCallbacks {
    val currentCallbacks by rememberUpdatedState(screenCallbacks)

    return remember(contentUiState, snackbarHostState) {
        val onShownPhoto: (Picture) -> Unit = { picture: Picture ->
            contentUiState.visible = true
            contentUiState.baseUiState.scope.launch {
                contentUiState.enabledBookmark = contentUiState.bookmarkViewModel.isPhotoBookmarked(picture)
            }
        }

        PictureViewerInternalCallbacks(
            viewerCallbacks = PictureViewerCallbacks(
                userContainerCallbacks = UserContainerCallbacks(
                    onFollowClick = { currentCallbacks.onToggleFollow(it) },
                    onShowUserInfo = { contentUiState.selectedUser = it },
                    onViewPhotos = { name, first, last, user -> currentCallbacks.onViewPhotos(name, first, last, user) }
                ),
                onShownPhoto = onShownPhoto,
                onSuccess = { isSuccessful ->
                    if (!isSuccessful) contentUiState.visible = false
                },
                onDownloadTriggered = { url ->
                    contentUiState.baseUiState.scope.launch {
                        contentUiState.photoDownloadViewModel.getDownloadPhotoUrl(url)
                        contentUiState.showPopup = true
                    }
                },
                onDownloadPhoto = { url ->
                    contentUiState.baseUiState.scope.launch {
                        contentUiState.photoDownloadViewModel.downloadPhoto(url, contentUiState.id)
                            .onSuccess {
                                contentUiState.dialogState = DownloadDialogState.Success
                                contentUiState.showPopup = false
                            }
                            .onFailure { error ->
                                contentUiState.dialogState =
                                    if (error is PhotoAlreadyExistsException) {
                                        DownloadDialogState.Duplicate
                                    } else {
                                        DownloadDialogState.Error(
                                            error.message ?: contentUiState.baseUiState.context.getString(R.string.error_unknown)
                                        )
                                    }
                                contentUiState.showPopup = false
                            }
                    }
                },
                onRetry = {
                    contentUiState.pictureViewModel.loadPicture(contentUiState.id)
                },
                onDismissPopup = { contentUiState.showPopup = false },
                onDismissDialog = {
                    contentUiState.dialogState = DownloadDialogState.Idle
                    contentUiState.showPopup = false
                }
            ),
            onLikedClick = { contentUiState.pictureViewModel.toggleLike() },
            onBookmarkClick = { picture ->
                picture.bookmarked = !contentUiState.enabledBookmark
                contentUiState.bookmarkViewModel.setBookmarkPicture(picture)
                contentUiState.enabledBookmark = !contentUiState.enabledBookmark
            },
            onBackPressed = { currentCallbacks.onBackPressed() }
        )
    }
}