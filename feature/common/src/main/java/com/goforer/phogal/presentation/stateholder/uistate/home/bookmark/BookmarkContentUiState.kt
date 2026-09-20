package com.goforer.phogal.presentation.stateholder.uistate.home.bookmark

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.goforer.phogal.presentation.stateholder.uistate.home.common.base.BasePhotoContentUiState

@Stable
data class BookmarkCallbacks(
    // Query
    val isUserFollowed: (User) -> Boolean,

    // Action
    val onToggleFollow: (User) -> Unit,
    val onShowUserInfo: (User) -> Unit,
    val onItemClicked: (item: Picture, index: Int) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onLoadResult: (PagingResult) -> Unit
)

@Stable
data class BookmarkScreenCallbacks(
    // Query
    val isUserFollowed: (User) -> Boolean,

    // Action
    val onItemClicked: (item: Picture, index: Int) -> Unit,
    val onBackPressed: () -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onOpenWebView: (firstName: String, url: String) -> Unit,
    val onToggleFollow: (User) -> Unit,
    val onStart: () -> Unit = {},
    val onStop: () -> Unit = {}
)

@Stable
class BookmarkContentUiState internal constructor(
    override val baseUiState: BaseUiState,
    val photos: LazyPagingItems<Picture>,
    initialEnabledLoadPhotos: Boolean,
    initialError: ErrorEntity?,
    initialSelectedUser: User?,
) : BasePhotoContentUiState(
    baseUiState = baseUiState,
    initialVisible = false,
    initialError = initialError,
    initialSelectedUser = initialSelectedUser,
) {
    var enabledLoadPhotos: Boolean by mutableStateOf(initialEnabledLoadPhotos)

    companion object {
        fun Saver(baseUiState: BaseUiState, bookmarkedPictures: LazyPagingItems<Picture>): Saver<BookmarkContentUiState, *> = Saver(
            save = {
                listOf(
                    it.enabledLoadPhotos,
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
                val enabledLoadPhotos = it[0] as Boolean
                val errorMap = it[1] as? Map<*, *>
                val selectedUserStr = it[2] as? String
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
                BookmarkContentUiState(
                    baseUiState = baseUiState,
                    photos = bookmarkedPictures,
                    initialEnabledLoadPhotos = enabledLoadPhotos,
                    initialError = error,
                    initialSelectedUser = selectedUserStr?.toUser()
                )
            }
        )
    }
}

@Composable
fun rememberBookmarkContentUiState(
    bookmarkViewModel: BookmarkViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    initialEnabledLoadPhotos: Boolean = true,
    initialError: ErrorEntity? = null,
    initialSelectedUser: User? = null
): BookmarkContentUiState {
    val photos = bookmarkViewModel.bookmarkedPictures.collectAsLazyPagingItems()

    return rememberSaveable(
        baseUiState, photos,
        saver = BookmarkContentUiState.Saver(baseUiState, photos)
    ) {
        BookmarkContentUiState(
            baseUiState = baseUiState,
            photos = photos,
            initialEnabledLoadPhotos = initialEnabledLoadPhotos,
            initialError = initialError,
            initialSelectedUser = initialSelectedUser
        )
    }
}

/**
 * A helper structure to hold internal UI logic and stable callbacks.
 */
@Stable
class BookmarkInternalCallbacks internal constructor(
    val callbacks: BookmarkCallbacks
)

@Composable
fun rememberBookmarkInternalCallbacks(
    contentUiState: BookmarkContentUiState,
    screenCallbacks: BookmarkScreenCallbacks
): BookmarkInternalCallbacks {
    val currentCallbacks by rememberUpdatedState(screenCallbacks)

    return remember(contentUiState) {
        val onLoadResultStable: (PagingResult) -> Unit = { result ->
            when (result) {
                is PagingResult.Success -> {
                    contentUiState.enabledLoadPhotos = true
                }
                is PagingResult.Error -> {
                    contentUiState.enabledLoadPhotos = false
                    contentUiState.error = result.error
                }
                else -> {}
            }
        }

        BookmarkInternalCallbacks(
            callbacks = BookmarkCallbacks(
                isUserFollowed = { currentCallbacks.isUserFollowed(it) },
                onToggleFollow = { currentCallbacks.onToggleFollow(it) },
                onShowUserInfo = { contentUiState.selectedUser = it },
                onItemClicked = { item, index -> currentCallbacks.onItemClicked(item, index) },
                onViewPhotos = { name, first, last, user -> currentCallbacks.onViewPhotos(name, first, last, user) },
                onLoadResult = onLoadResultStable
            )
        )
    }
}