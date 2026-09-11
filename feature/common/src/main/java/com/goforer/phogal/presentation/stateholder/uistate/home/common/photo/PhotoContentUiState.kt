package com.goforer.phogal.presentation.stateholder.uistate.home.common.photo

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goforer.base.extension.toUser
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
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.viewer.DownloadDialogState
import kotlinx.coroutines.launch

@Stable
data class UserContainerActions(
    val onFollowClick: (User) -> Unit,
    val onShowUserInfo: (User) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit
)

@Stable
data class PictureViewerActions(
    val userContainerActions: UserContainerActions,
    val onShownPhoto: (picture: Picture) -> Unit,
    val onSuccess: (isSuccessful: Boolean) -> Unit,
    val onDownloadTriggered: (url: String) -> Unit,
    val onDownloadPhoto: (url: String) -> Unit,
    val onRetry: () -> Unit,
    val onDismissPopup: () -> Unit,
    val onDismissDialog: () -> Unit
)

@Stable
data class PictureViewerScreenActions(
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
    val baseUiState: BaseUiState,
    val pictureViewModel: PictureViewModel,
    val bookmarkViewModel: BookmarkViewModel,
    val photoDownloadViewModel: PhotoDownloadViewModel,
    val pictureState: UiState<Picture>,
    val trackDownloadState: UiState<TrackDownload>,

    private val _id: MutableState<String>,
    private val _showPopup: MutableState<Boolean>,
    private val _dialogState: MutableState<DownloadDialogState>,
    private val _visibleViewButton: MutableState<Boolean>,
    private val _enabledBookmark: MutableState<Boolean>,
    private val _visibleActions: MutableState<Boolean>,
    private val _selectedUser: MutableState<User?>,
) {
    val id: String get() = _id.value
    val showPopup: Boolean get() = _showPopup.value

    val dialogState: DownloadDialogState get() = _dialogState.value

    val visibleViewButton: Boolean get() = _visibleViewButton.value
    val enabledBookmark: Boolean get() = _enabledBookmark.value
    val visibleActions: Boolean get() = _visibleActions.value
    var selectedUser: User?
        get() = _selectedUser.value
        set(value) { _selectedUser.value = value }

    fun onId(id: String) {
        _id.value = id
    }

    fun setShowPopup(showPopup: Boolean) {
        _showPopup.value = showPopup
    }

    fun setDialogState(dialogState: DownloadDialogState) {
        _dialogState.value = dialogState
    }

    fun setVisibleViewButton(visibleViewButton: Boolean) {
        _visibleViewButton.value = visibleViewButton
    }

    fun setEnabledBookmark(enabledBookmark: Boolean) {
        _enabledBookmark.value = enabledBookmark
    }

    fun setVisibleActions(visibleActions: Boolean) {
        _visibleActions.value = visibleActions
    }
}

@Composable
fun rememberPhotoContentUiState(
    baseUiState: BaseUiState = rememberBaseUiState(),
    pictureViewModel: PictureViewModel,
    bookmarkViewModel: BookmarkViewModel,
    photoDownloadViewModel: PhotoDownloadViewModel,
    id: MutableState<String> = rememberSaveable { mutableStateOf("") },
    showPopup: MutableState<Boolean> = rememberSaveable() { mutableStateOf(false) },
    dialogState: MutableState<DownloadDialogState> = remember { mutableStateOf<DownloadDialogState>(DownloadDialogState.Idle) },
    visibleViewButton: MutableState<Boolean> = rememberSaveable() { mutableStateOf(false) },
    enabledBookmark: MutableState<Boolean> = rememberSaveable() { mutableStateOf(false) },
    visibleActions: MutableState<Boolean> = rememberSaveable() { mutableStateOf(false) },
    selectedUser: MutableState<User?> = rememberSaveable(
        saver = Saver(
            save = { it.value?.toString() },
            restore = { mutableStateOf(it?.toUser()) }
        )
    ) {
        mutableStateOf(null)
    }
): PhotoContentUiState {
    val pictureState by pictureViewModel.picture.collectAsStateWithLifecycle()
    val trackDownloadState by photoDownloadViewModel.trackDownload.collectAsStateWithLifecycle()

    return remember(
        baseUiState,
        pictureViewModel,
        bookmarkViewModel,
        photoDownloadViewModel,
        pictureState,
        trackDownloadState,
        id,
        showPopup,
        dialogState,
        visibleViewButton,
        enabledBookmark,
        visibleActions,
        selectedUser
    ) {
        PhotoContentUiState(
            baseUiState = baseUiState,
            pictureViewModel = pictureViewModel,
            bookmarkViewModel = bookmarkViewModel,
            photoDownloadViewModel = photoDownloadViewModel,
            pictureState = pictureState,
            trackDownloadState = trackDownloadState,
            _id = id,
            _showPopup = showPopup,
            _dialogState = dialogState,
            _visibleViewButton = visibleViewButton,
            _enabledBookmark = enabledBookmark,
            _visibleActions = visibleActions,
            _selectedUser = selectedUser
        )
    }
}

/**
 * A helper structure to hold internal UI logic and stable callbacks.
 */
@Stable
class PictureViewerInternalActions internal constructor(
    val viewerActions: PictureViewerActions,
    val onLikedClick: () -> Unit,
    val onBookmarkClick: (Picture) -> Unit,
    val onBackPressed: () -> Unit
)

@Composable
fun rememberPictureViewerInternalActions(
    contentUiState: PhotoContentUiState,
    screenActions: PictureViewerScreenActions,
    snackbarHostState: SnackbarHostState
): PictureViewerInternalActions {
    val currentActions by rememberUpdatedState(screenActions)

    return remember(contentUiState, snackbarHostState) {
        val onShownPhoto: (Picture) -> Unit = { picture: Picture ->
            contentUiState.setVisibleActions(true)
            contentUiState.baseUiState.scope.launch {
                contentUiState.setEnabledBookmark(contentUiState.bookmarkViewModel.isPhotoBookmarked(picture))
            }
        }

        PictureViewerInternalActions(
            viewerActions = PictureViewerActions(
                userContainerActions = UserContainerActions(
                    onFollowClick = { currentActions.onToggleFollow(it) },
                    onShowUserInfo = { contentUiState.selectedUser = it },
                    onViewPhotos = { name, first, last, user -> currentActions.onViewPhotos(name, first, last, user) }
                ),
                onShownPhoto = onShownPhoto,
                onSuccess = { isSuccessful ->
                    if (!isSuccessful) contentUiState.setVisibleActions(visibleActions = false)
                },
                onDownloadTriggered = { url ->
                    contentUiState.baseUiState.scope.launch {
                        contentUiState.photoDownloadViewModel.getDownloadPhotoUrl(url)
                        contentUiState.setShowPopup(true)
                    }
                },
                onDownloadPhoto = { url ->
                    contentUiState.baseUiState.scope.launch {
                        contentUiState.photoDownloadViewModel.downloadPhoto(url, contentUiState.id)
                            .onSuccess {
                                contentUiState.setDialogState(DownloadDialogState.Success)
                                contentUiState.setShowPopup(false)
                            }
                            .onFailure { error ->
                                contentUiState.setDialogState(
                                    if (error is PhotoAlreadyExistsException) {
                                        DownloadDialogState.Duplicate
                                    } else {
                                        DownloadDialogState.Error(
                                            error.message ?: contentUiState.baseUiState.context.getString(R.string.error_unknown)
                                        )
                                    }
                                )
                                contentUiState.setShowPopup(false)
                            }
                    }
                },
                onRetry = {
                    contentUiState.pictureViewModel.loadPicture(contentUiState.id)
                },
                onDismissPopup = { contentUiState.setShowPopup(false) },
                onDismissDialog = {
                    contentUiState.setDialogState(DownloadDialogState.Idle)
                    contentUiState.setShowPopup(false)
                }
            ),
            onLikedClick = { contentUiState.pictureViewModel.toggleLike() },
            onBookmarkClick = { picture ->
                picture.bookmarked = !contentUiState.enabledBookmark
                contentUiState.bookmarkViewModel.setBookmarkPicture(picture)
                contentUiState.setEnabledBookmark(!contentUiState.enabledBookmark)
            },
            onBackPressed = { currentActions.onBackPressed() }
        )
    }
}
