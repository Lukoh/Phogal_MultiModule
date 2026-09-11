package com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.base.extension.toUser
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.business.home.popularphotos.PopularPhotosViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.ErrorEntity
import com.goforer.phogal.presentation.stateholder.uistate.PagingResult
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
data class PopularPhotosActions(
    val isUserFollowed: (User) -> Boolean,
    val onToggleFollow: (User) -> Unit,
    val onShowUserInfo: (User) -> Unit,
    val onItemClicked: (id: String, index: Int) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onLoadResult: (PagingResult) -> Unit,
    val onLoadedPhotos: (isLoadedPhotos: Boolean) -> Unit
)

@Stable
data class PopularPhotosScreenActions(
    val isUserFollowed: (User) -> Boolean,
    val onToggleFollow: (User) -> Unit,
    val onItemClicked: (id: String, index: Int) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onOpenWebView: (firstName: String, url: String) -> Unit,
    val onStart: () -> Unit = {},
    val onStop: () -> Unit = {}
)

@Stable
class PopularPhotosContentUiState internal constructor(
    val baseUiState: BaseUiState,
    val photos: LazyPagingItems<Photo>,

    private val _visibleActions: MutableState<Boolean>,
    private val _loadedPhotos: MutableState<Boolean>,
    private val _error: MutableState<ErrorEntity?>,
    private val _selectedUser: MutableState<User?>,
) {
    val visibleActions: Boolean get() = _visibleActions.value
    val loadedPhotos: Boolean get() = _loadedPhotos.value
    val error: ErrorEntity? get() = _error.value
    var selectedUser: User?
        get() = _selectedUser.value
        set(value) { _selectedUser.value = value }

    fun setVisibleActions(visibleActions: Boolean) {
        _visibleActions.value = visibleActions
    }

    fun setLoadedPhotos(loadedPhotos: Boolean) {
        _loadedPhotos.value = loadedPhotos
    }

    fun setError(error: ErrorEntity?) {
        _error.value = error
    }
}

@Composable
fun rememberPopularPhotosContentUiState(
    popularPhotosViewModel: PopularPhotosViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    visibleActions: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
    loadedPhotos: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) },
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
): PopularPhotosContentUiState {
    val photos = popularPhotosViewModel.photos.collectAsLazyPagingItems()

    return remember(popularPhotosViewModel, baseUiState, visibleActions, loadedPhotos, error, selectedUser) {
        PopularPhotosContentUiState(
            baseUiState = baseUiState,
            photos = photos,
            _visibleActions = visibleActions,
            _loadedPhotos = loadedPhotos,
            _error = error,
            _selectedUser = selectedUser
        )
    }
}

/**
 * A helper structure to hold internal UI logic and stable callbacks.
 */
@Stable
class PopularPhotosInternalActions internal constructor(
    val actions: PopularPhotosActions
)

@Composable
fun rememberPopularPhotosInternalActions(
    contentUiState: PopularPhotosContentUiState,
    screenActions: PopularPhotosScreenActions
): PopularPhotosInternalActions {
    val currentActions by rememberUpdatedState(screenActions)

    return remember(contentUiState) {
        val onLoadResultStable: (PagingResult) -> Unit = { result ->
            if (result is PagingResult.Success || result is PagingResult.Error) {
                contentUiState.setVisibleActions(result is PagingResult.Success)
            }
            if (result is PagingResult.Error) {
                contentUiState.setError(result.error)
            }
        }

        PopularPhotosInternalActions(
            actions = PopularPhotosActions(
                isUserFollowed = { currentActions.isUserFollowed(it) },
                onToggleFollow = { currentActions.onToggleFollow(it) },
                onShowUserInfo = { contentUiState.selectedUser = it },
                onItemClicked = { id, index -> currentActions.onItemClicked(id, index) },
                onViewPhotos = { name, first, last, user -> currentActions.onViewPhotos(name, first, last, user) },
                onLoadResult = onLoadResultStable,
                onLoadedPhotos = { contentUiState.setLoadedPhotos(it) }
            )
        )
    }
}
