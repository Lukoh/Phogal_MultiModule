package com.goforer.phogal.presentation.stateholder.uistate.home.gallery

import android.Manifest
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.goforer.base.extension.toUser
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.business.home.gallery.GalleryViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.ErrorEntity
import com.goforer.phogal.presentation.stateholder.uistate.PagingResult
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.base.BasePhotoContentUiState

@Stable
data class SearchPhotosCallbacks(
    // Query
    val isUserFollowed: (String) -> Boolean,
    val isPhotoBookmarked: (String) -> Boolean,

    // Action
    val onPerformSearch: (keyword: String, isFromChip: Boolean) -> Unit,
    val onToggleFollow: (User) -> Unit,
    val onShowUserInfo: (User) -> Unit,
    val onItemClicked: (id: String) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onLoadResult: (PagingResult) -> Unit,
    val onScroll: (Boolean) -> Unit,
    val onMenuClick: () -> Unit,
    val onFavoriteClick: () -> Unit
)

@Stable
data class SearchPhotosScreenCallbacks(
    // Query
    val isUserFollowed: (String) -> Boolean,
    val isPhotoBookmarked: (String) -> Boolean,

    // Action
    val onToggleFollow: (User) -> Unit,
    val onItemClicked: (id: String) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onOpenWebView: (firstName: String, url: String) -> Unit,
    val onStart: () -> Unit = {},
    val onStop: () -> Unit = {}
)

@Stable
class SearchPhotoContentUiState(
    override val baseUiState: BaseUiState,
    val galleryViewModel: GalleryViewModel,
    initialEnabled: Boolean,
    initialTriggered: Boolean,
    initialPermissionVisible: Boolean,
    initialRationaleText: String,
    initialScrolling: Boolean,
    initialVisible: Boolean,
    initialError: ErrorEntity?,
    initialSelectedUser: User?,
) : BasePhotoContentUiState(
    baseUiState = baseUiState,
    initialVisible = initialVisible,
    initialError = initialError,
    initialSelectedUser = initialSelectedUser
) {
    var enabled: Boolean by mutableStateOf(initialEnabled)
    var triggered: Boolean by mutableStateOf(initialTriggered)
    var permissionVisible: Boolean by mutableStateOf(initialPermissionVisible)
    var rationaleText: String by mutableStateOf(initialRationaleText)
    var scrolling: Boolean by mutableStateOf(initialScrolling)

    val permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CAMERA
    )

    companion object {
        fun Saver(
            baseUiState: BaseUiState,
            galleryViewModel: GalleryViewModel
        ): Saver<SearchPhotoContentUiState, *> = Saver(
            save = {
                listOf(
                    it.enabled,
                    it.triggered,
                    it.permissionVisible,
                    it.rationaleText,
                    it.scrolling,
                    it.visible,
                    it.error?.let { err ->
                        mapOf(
                            "type" to err::class.simpleName,
                            "message" to err.message,
                            "code" to (err as? ErrorEntity.Network)?.code
                        )
                    },
                    it.selectedUser?.toString()
                )
            },
            restore = {
                val enabled = it[0] as Boolean
                val triggered = it[1] as Boolean
                val permissionVisible = it[2] as Boolean
                val rationaleText = it[3] as String
                val scrolling = it[4] as Boolean
                val visible = it[5] as Boolean
                val errorMap = it[6] as? Map<*, *>
                val selectedUserStr = it[7] as? String
                val error = errorMap?.let { map ->
                    val type = map["type"] as? String
                    val message = map["message"] as? String ?: ""
                    val code = map["code"] as? Int ?: 0
                    when (type) {
                        "Network" -> ErrorEntity.Network(code, message)
                        "Persistence" -> ErrorEntity.Persistence(message)
                        "Permission" -> ErrorEntity.Permission(message)
                        "Unknown" -> ErrorEntity.Unknown(message)
                        else -> null
                    }
                }
                SearchPhotoContentUiState(
                    baseUiState = baseUiState,
                    galleryViewModel = galleryViewModel,
                    initialEnabled = enabled,
                    initialTriggered = triggered,
                    initialPermissionVisible = permissionVisible,
                    initialRationaleText = rationaleText,
                    initialScrolling = scrolling,
                    initialVisible = visible,
                    initialError = error,
                    initialSelectedUser = selectedUserStr?.toUser()
                )
            }
        )
    }
}

@Composable
fun rememberSearchPhotosContentUiState(
    galleryViewModel: GalleryViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    initialEnabled: Boolean = false,
    initialTriggered: Boolean = false,
    initialPermissionVisible: Boolean = false,
    initialRationaleText: String = "",
    initialScrolling: Boolean = false,
    initialVisible: Boolean = true,
    initialError: ErrorEntity? = null,
    initialSelectedUser: User? = null
): SearchPhotoContentUiState {
    return rememberSaveable(
        baseUiState, galleryViewModel,
        saver = SearchPhotoContentUiState.Saver(baseUiState, galleryViewModel)
    ) {
        SearchPhotoContentUiState(
            galleryViewModel = galleryViewModel,
            baseUiState = baseUiState,
            initialEnabled = initialEnabled,
            initialTriggered = initialTriggered,
            initialPermissionVisible = initialPermissionVisible,
            initialRationaleText = initialRationaleText,
            initialScrolling = initialScrolling,
            initialVisible = initialVisible,
            initialError = initialError,
            initialSelectedUser = initialSelectedUser
        )
    }
}

/**
 * A helper structure to hold internal UI logic and stable callbacks.
 */
@Stable
class SearchPhotosInternalCallbacks internal constructor(
    val callbacks: SearchPhotosCallbacks,
    val onMenuClick: () -> Unit,
    val onFavoriteClick: () -> Unit
)

@Composable
fun rememberSearchPhotosInternalCallbacks(
    contentUiState: SearchPhotoContentUiState,
    sectionUiState: SearchSectionUiState,
    screenCallbacks: SearchPhotosScreenCallbacks,
    snackbarHostState: SnackbarHostState
): SearchPhotosInternalCallbacks {
    val currentCallbacks by rememberUpdatedState(screenCallbacks)

    return remember(contentUiState, sectionUiState, snackbarHostState) {
        val performSearch: (String, Boolean) -> Unit = { keyword, shouldTrigger ->
            val currentQuery = contentUiState.galleryViewModel.query.value

            if (keyword.isNotBlank() && (keyword != currentQuery)) {
                contentUiState.baseUiState.keyboardController?.hide()
                contentUiState.galleryViewModel.commitSearch(keyword)
                if (shouldTrigger) {
                    contentUiState.triggered = true
                }
            }
        }

        val onLoadResultStable: (PagingResult) -> Unit = { result ->
            if (result is PagingResult.Success || result is PagingResult.Error) {
                contentUiState.visible = result is PagingResult.Success
            }
            if (result is PagingResult.Error) {
                contentUiState.error = result.error
            }
        }

        val onPerformSearch: (String, Boolean) -> Unit = { keyword, isFromChip ->
            performSearch(keyword, !isFromChip)
        }

        val onMenuClick: () -> Unit = { /* TODO */ }
        val onFavoriteClick: () -> Unit = { /* TODO */ }

        SearchPhotosInternalCallbacks(
            callbacks = SearchPhotosCallbacks(
                onPerformSearch = onPerformSearch,
                isUserFollowed = { currentCallbacks.isUserFollowed(it) },
                isPhotoBookmarked = { currentCallbacks.isPhotoBookmarked(it) },
                onToggleFollow = { currentCallbacks.onToggleFollow(it) },
                onShowUserInfo = { contentUiState.selectedUser = it },
                onItemClicked = { currentCallbacks.onItemClicked(it) },
                onViewPhotos = { name, first, last, user -> currentCallbacks.onViewPhotos(name, first, last, user) },
                onLoadResult = onLoadResultStable,
                onScroll = { contentUiState.scrolling = it },
                onMenuClick = onMenuClick,
                onFavoriteClick = onFavoriteClick
            ),
            onMenuClick = onMenuClick,
            onFavoriteClick = onFavoriteClick
        )
    }
}