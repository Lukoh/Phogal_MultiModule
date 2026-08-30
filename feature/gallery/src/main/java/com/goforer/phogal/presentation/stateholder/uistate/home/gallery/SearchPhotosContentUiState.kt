package com.goforer.phogal.presentation.stateholder.uistate.home.gallery

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.presentation.stateholder.business.home.gallery.GalleryViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
class GalleryUiState internal constructor(
    val photos: LazyPagingItems<Photo>,

    private val currentQueryProvider: () -> String,
    private val recentWordsProvider: () -> List<String>
) {
    val currentQuery: String get() = currentQueryProvider()
    val recentWords: List<String> get() = recentWordsProvider()
}

@Stable
class SearchPhotosContentUiState internal constructor(
    val baseUiState: BaseUiState,
    val galleryUiState: GalleryUiState,
    val galleryViewModel: GalleryViewModel,

    private val _enabled: MutableState<Boolean>,
    private val _triggered: MutableState<Boolean>,
    private val _permissionVisible: MutableState<Boolean>,
    private val _rationaleText: MutableState<String>,
    private val _scrolling: MutableState<Boolean>,
    private val _visibleActions: MutableState<Boolean>,
) {
    val enabled: Boolean get() = _enabled.value
    val triggered: Boolean get() = _triggered.value
    val permissionVisible: Boolean get() = _permissionVisible.value
    val rationaleText: String get() = _rationaleText.value
    val scrolling: Boolean get() = _scrolling.value
    val visibleActions: Boolean get() = _visibleActions.value

    fun setPermissionGranted() {
        _enabled.value = true;
        _permissionVisible.value = false
    }
    fun setPermissionDenied(rationale: String) {
        _rationaleText.value = rationale;
        _enabled.value = false;
        _permissionVisible.value = true
    }
    fun setPermissionDialogDismissed() {
        _enabled.value = false;
        _permissionVisible.value = false
    }
    fun setPermissionDialogConfirmed() { _permissionVisible.value = false }
    fun setTriggerConsumed() { _triggered.value = false }
    fun setSearchTriggered() { _triggered.value = true }
    fun setScrollingChanged(scrolling: Boolean) { _scrolling.value = scrolling }
    fun setActionsVisibilityChanged(visible: Boolean) { _visibleActions.value = visible }
    fun setEnabled(enabled: Boolean) { _enabled.value = enabled }

    val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )
}

@Composable
fun rememberSearchPhotosContentUiState(
    galleryViewModel: GalleryViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    enabled: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    triggered: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    permissionVisible: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    rationaleText: MutableState<String> = rememberSaveable { mutableStateOf("") },
    scrolling: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
    visibleActions: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) }
): SearchPhotosContentUiState {
    val photos = galleryViewModel.photos.collectAsLazyPagingItems()
    val currentQuery by galleryViewModel.query.collectAsStateWithLifecycle()
    val recentWords by galleryViewModel.recentWords.collectAsStateWithLifecycle()
    val galleryUiState = remember(galleryViewModel, currentQuery, recentWords) {
        GalleryUiState(
            photos = photos,
            currentQueryProvider = { currentQuery },
            recentWordsProvider = { recentWords }
        )
    }

    return remember(
        galleryViewModel,
        baseUiState,
        enabled,
        triggered,
        permissionVisible,
        rationaleText,
        scrolling,
        visibleActions
    ) {
        SearchPhotosContentUiState(
            galleryViewModel = galleryViewModel,
            baseUiState = baseUiState,
            galleryUiState = galleryUiState,
            _enabled = enabled,
            _triggered = triggered,
            _permissionVisible = permissionVisible,
            _rationaleText = rationaleText,
            _scrolling = scrolling,
            _visibleActions = visibleActions
        )
    }
}
