package com.goforer.phogal.presentation.stateholder.uistate.home.gallery

import android.Manifest
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
import com.goforer.phogal.presentation.stateholder.business.home.gallery.GalleryViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.ErrorEntity
import com.goforer.phogal.presentation.stateholder.uistate.PagingResult
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.base.extension.toUser

@Stable
data class SearchPhotosActions(
    val onPerformSearch: (keyword: String, isFromChip: Boolean) -> Unit,
    val isUserFollowed: (User) -> Boolean,
    val onToggleFollow: (User) -> Unit,
    val onShowUserInfo: (User) -> Unit,
    val onItemClicked: (id: String) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onLoadResult: (PagingResult) -> Unit
)

@Stable
data class SearchPhotosScreenActions(
    val isUserFollowed: (User) -> Boolean,
    val onToggleFollow: (User) -> Unit,
    val onItemClicked: (id: String) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onOpenWebView: (firstName: String, url: String) -> Unit,
    val onStart: () -> Unit = {},
    val onStop: () -> Unit = {}
)

@Stable
class SearchPhotosContentUiState internal constructor(
    val baseUiState: BaseUiState,
    val galleryViewModel: GalleryViewModel,

    private val _enabled: MutableState<Boolean>,
    private val _triggered: MutableState<Boolean>,
    private val _permissionVisible: MutableState<Boolean>,
    private val _rationaleText: MutableState<String>,
    private val _scrolling: MutableState<Boolean>,
    private val _visibleActions: MutableState<Boolean>,
    private val _error: MutableState<ErrorEntity?>,
    private val _selectedUser: MutableState<User?>,
) {
    val enabled: Boolean get() = _enabled.value
    val triggered: Boolean get() = _triggered.value
    val permissionVisible: Boolean get() = _permissionVisible.value
    val rationaleText: String get() = _rationaleText.value
    val scrolling: Boolean get() = _scrolling.value
    val visibleActions: Boolean get() = _visibleActions.value
    val error: ErrorEntity? get() = _error.value
    var selectedUser: User?
        get() = _selectedUser.value
        set(value) { _selectedUser.value = value }

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
    fun setError(error: ErrorEntity?) { _error.value = error }

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
    visibleActions: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    error: MutableState<ErrorEntity?> = rememberSaveable(
        saver = Saver(
            save = { state ->
                state.value?.let {
                    mapOf(
                        "type" to it::class.simpleName,
                        "message" to it.message,
                        "code" to (it as? ErrorEntity.Network)?.code
                    )
                }
            },
            restore = { map ->
                val type = map["type"] as? String
                val message = map["message"] as? String ?: ""
                val code = map["code"] as? Int ?: 0
                mutableStateOf(
                    when (type) {
                        "Network" -> ErrorEntity.Network(code, message)
                        "Persistence" -> ErrorEntity.Persistence(message)
                        "Permission" -> ErrorEntity.Permission(message)
                        "Unknown" -> ErrorEntity.Unknown(message)
                        else -> null
                    }
                )
            }
        )
    ) {
        mutableStateOf(null)
    },
    selectedUser: MutableState<User?> = rememberSaveable(
        saver = Saver(
            save = { it.value?.toString() },
            restore = { mutableStateOf(it?.toUser()) }
        )
    ) {
        mutableStateOf(null)
    }
): SearchPhotosContentUiState {
    return remember(
        galleryViewModel,
        baseUiState,
        enabled,
        triggered,
        permissionVisible,
        rationaleText,
        scrolling,
        visibleActions,
        error,
        selectedUser
    ) {
        SearchPhotosContentUiState(
            galleryViewModel = galleryViewModel,
            baseUiState = baseUiState,
            _enabled = enabled,
            _triggered = triggered,
            _permissionVisible = permissionVisible,
            _rationaleText = rationaleText,
            _scrolling = scrolling,
            _visibleActions = visibleActions,
            _error = error,
            _selectedUser = selectedUser
        )
    }
}

/**
 * A helper structure to hold internal UI logic and stable callbacks.
 */
@Stable
class SearchPhotosInternalActions internal constructor(
    val actions: SearchPhotosActions,
    val onMenuClick: () -> Unit,
    val onFavoriteClick: () -> Unit
)

@Composable
fun rememberSearchPhotosInternalActions(
    contentUiState: SearchPhotosContentUiState,
    sectionUiState: SearchSectionUiState,
    screenActions: SearchPhotosScreenActions,
    snackbarHostState: SnackbarHostState
): SearchPhotosInternalActions {
    val currentActions by rememberUpdatedState(screenActions)

    return remember(contentUiState, sectionUiState, snackbarHostState) {
        val performSearch: (String, Boolean) -> Unit = { keyword, shouldTrigger ->
            val currentQuery = contentUiState.galleryViewModel.query.value

            if (keyword.isNotBlank() && (keyword != currentQuery)) {
                contentUiState.baseUiState.keyboardController?.hide()
                contentUiState.galleryViewModel.commitSearch(keyword)
                if (shouldTrigger) {
                    contentUiState.setSearchTriggered()
                }
            }
        }

        val onLoadResultStable: (PagingResult) -> Unit = { result ->
            if (result is PagingResult.Success || result is PagingResult.Error) {
                contentUiState.setActionsVisibilityChanged(result is PagingResult.Success)
            }
            if (result is PagingResult.Error) {
                contentUiState.setError(result.error)
            }
        }

        val onPerformSearch: (String, Boolean) -> Unit = { keyword, isFromChip ->
            performSearch(keyword, !isFromChip)
        }

        SearchPhotosInternalActions(
            actions = SearchPhotosActions(
                onPerformSearch = onPerformSearch,
                isUserFollowed = { currentActions.isUserFollowed(it) },
                onToggleFollow = { currentActions.onToggleFollow(it) },
                onShowUserInfo = { contentUiState.selectedUser = it },
                onItemClicked = { currentActions.onItemClicked(it) },
                onViewPhotos = { name, first, last, user -> currentActions.onViewPhotos(name, first, last, user) },
                onLoadResult = onLoadResultStable
            ),
            onMenuClick = { /* TODO */ },
            onFavoriteClick = { /* TODO */ }
        )
    }
}
