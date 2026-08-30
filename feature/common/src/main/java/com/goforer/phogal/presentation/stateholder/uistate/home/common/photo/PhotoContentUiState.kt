package com.goforer.phogal.presentation.stateholder.uistate.home.common.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goforer.phogal.data.model.remote.response.gallery.photo.download.TrackDownload
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.business.home.common.photo.info.PictureViewModel
import com.goforer.phogal.presentation.stateholder.business.home.download.PhotoDownloadViewModel
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.UiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.viewer.DownloadDialogState

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
    private val _visibleActions: MutableState<Boolean>
) {
    val id: String get() = _id.value
    val showPopup: Boolean get() = _showPopup.value

    val dialogState: DownloadDialogState get() = _dialogState.value

    val visibleViewButton: Boolean get() = _visibleViewButton.value
    val enabledBookmark: Boolean get() = _enabledBookmark.value
    val visibleActions: Boolean get() = _visibleActions.value

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
    visibleActions: MutableState<Boolean> = rememberSaveable() { mutableStateOf(false) }
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
        visibleActions) {
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
            _visibleActions = visibleActions
        )
    }
}
