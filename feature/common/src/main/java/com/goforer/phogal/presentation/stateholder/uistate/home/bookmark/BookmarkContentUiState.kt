package com.goforer.phogal.presentation.stateholder.uistate.home.bookmark

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
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.ErrorEntity
import com.goforer.phogal.presentation.stateholder.uistate.PagingResult
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
data class BookmarkActions(
    val isUserFollowed: (User) -> Boolean,
    val onToggleFollow: (User) -> Unit,
    val onShowUserInfo: (User) -> Unit,
    val onItemClicked: (item: Picture, index: Int) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onLoadResult: (PagingResult) -> Unit
)

@Stable
data class BookmarkScreenActions(
    val onItemClicked: (item: Picture, index: Int) -> Unit,
    val onBackPressed: () -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onOpenWebView: (firstName: String, url: String) -> Unit,
    val isUserFollowed: (User) -> Boolean,
    val onToggleFollow: (User) -> Unit,
    val onStart: () -> Unit = {},
    val onStop: () -> Unit = {}
)

@Stable
class BookmarkContentUiState internal constructor(
    val baseUiState: BaseUiState,
    val bookmarkedPictures: LazyPagingItems<Picture>,

    private val _enabledLoadPhotos: MutableState<Boolean>,
    private val _error: MutableState<ErrorEntity?>,
    private val _selectedUser: MutableState<User?>,
) {
    val enabledLoadPhotos: Boolean get() = _enabledLoadPhotos.value
    val error: ErrorEntity? get() = _error.value
    var selectedUser: User?
        get() = _selectedUser.value
        set(value) { _selectedUser.value = value }

    fun setEnabledLoadPhotos(enabled: Boolean) {
        _enabledLoadPhotos.value = enabled
    }

    fun setError(error: ErrorEntity?) {
        _error.value = error
    }
}

@Composable
fun rememberBookmarkContentUiState(
    bookmarkViewModel: BookmarkViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    enabledLoadPhotos: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) },
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
            restore = { mutableStateOf(it.toUser()) }
        )
    ) {
        mutableStateOf(null)
    }
): BookmarkContentUiState {
    val bookmarkedPictures = bookmarkViewModel.bookmarkedPictures.collectAsLazyPagingItems()

    return remember(baseUiState, bookmarkViewModel, enabledLoadPhotos, error, selectedUser) {
        BookmarkContentUiState(
            baseUiState = baseUiState,
            bookmarkedPictures = bookmarkedPictures,
            _enabledLoadPhotos = enabledLoadPhotos,
            _error = error,
            _selectedUser = selectedUser
        )
    }
}

/**
 * A helper structure to hold internal UI logic and stable callbacks.
 */
@Stable
class BookmarkInternalActions internal constructor(
    val actions: BookmarkActions
)

@Composable
fun rememberBookmarkInternalActions(
    contentUiState: BookmarkContentUiState,
    screenActions: BookmarkScreenActions
): BookmarkInternalActions {
    val currentActions by rememberUpdatedState(screenActions)

    return remember(contentUiState) {
        val onLoadResultStable: (PagingResult) -> Unit = { result ->
            when (result) {
                is PagingResult.Success -> {
                    contentUiState.setEnabledLoadPhotos(true)
                }
                is PagingResult.Error -> {
                    contentUiState.setEnabledLoadPhotos(false)
                    contentUiState.setError(result.error)
                }
                else -> {}
            }
        }

        BookmarkInternalActions(
            actions = BookmarkActions(
                isUserFollowed = { currentActions.isUserFollowed(it) },
                onToggleFollow = { currentActions.onToggleFollow(it) },
                onShowUserInfo = { contentUiState.selectedUser = it },
                onItemClicked = { item, index -> currentActions.onItemClicked(item, index) },
                onViewPhotos = { name, first, last, user -> currentActions.onViewPhotos(name, first, last, user) },
                onLoadResult = onLoadResultStable
            )
        )
    }
}
