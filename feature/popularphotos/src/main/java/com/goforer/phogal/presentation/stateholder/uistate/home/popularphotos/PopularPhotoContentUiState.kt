package com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos

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
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.business.home.popularphotos.PopularPhotosViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.ErrorEntity
import com.goforer.phogal.presentation.stateholder.uistate.PagingResult
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.base.BasePhotoContentUiState

@Stable
data class PopularPhotosCallbacks(
    // Query
    val isUserFollowed: (String) -> Boolean,
    val isPhotoBookmarked: (String) -> Boolean,

    // Action
    val onToggleFollow: (User) -> Unit,
    val onShowUserInfo: (User) -> Unit,
    val onItemClicked: (id: String, index: Int) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onLoadResult: (PagingResult) -> Unit,
    val onLoadedPhotos: (isLoadedPhotos: Boolean) -> Unit
)

@Stable
data class PopularPhotosScreenCallbacks(
    // Query
    val isUserFollowed: (String) -> Boolean,
    val isPhotoBookmarked: (String) -> Boolean,

    // Action
    val onToggleFollow: (User) -> Unit,
    val onItemClicked: (id: String, index: Int) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onOpenWebView: (firstName: String, url: String) -> Unit,
    val onStart: () -> Unit = {},
    val onStop: () -> Unit = {}
)

@Stable
class PopularPhotoContentUiState internal constructor(
    override val baseUiState: BaseUiState,
    val popularPhotosViewModel: PopularPhotosViewModel,
    val photos: LazyPagingItems<Photo>,
    initialVisible: Boolean,
    initialLoadedPhotos: Boolean,
    initialError: ErrorEntity?,
    initialSelectedUser: User?,
) : BasePhotoContentUiState(
    baseUiState = baseUiState,
    initialVisible = initialVisible,
    initialError = initialError,
    initialSelectedUser = initialSelectedUser,
) {
    var loadedPhotos: Boolean by mutableStateOf(initialLoadedPhotos)

    companion object {
        fun Saver(
            baseUiState: BaseUiState,
            popularPhotosViewModel: PopularPhotosViewModel,
            photos: LazyPagingItems<Photo>
        ): Saver<PopularPhotoContentUiState, *> = Saver(
            save = {
                listOf(
                    it.visible,
                    it.loadedPhotos,
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
                val visible = it[0] as Boolean
                val loadedPhotos = it[1] as Boolean
                val errorMap = it[2] as? Map<*, *>
                val selectedUserStr = it[3] as? String
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
                PopularPhotoContentUiState(
                    baseUiState = baseUiState,
                    popularPhotosViewModel = popularPhotosViewModel,
                    photos = photos,
                    initialVisible = visible,
                    initialLoadedPhotos = loadedPhotos,
                    initialError = error,
                    initialSelectedUser = selectedUserStr?.toUser()
                )
            }
        )
    }
}

@Composable
fun rememberPopularPhotosContentUiState(
    popularPhotosViewModel: PopularPhotosViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    initialVisible: Boolean = true,
    initialLoadedPhotos: Boolean = false,
    initialError: ErrorEntity? = null,
    initialSelectedUser: User? = null
): PopularPhotoContentUiState {
    val photos = popularPhotosViewModel.photos.collectAsLazyPagingItems()

    return rememberSaveable(
        baseUiState, photos, popularPhotosViewModel,
        saver = PopularPhotoContentUiState.Saver(baseUiState, popularPhotosViewModel, photos)
    ) {
        PopularPhotoContentUiState(
            baseUiState = baseUiState,
            popularPhotosViewModel = popularPhotosViewModel,
            photos = photos,
            initialVisible = initialVisible,
            initialLoadedPhotos = initialLoadedPhotos,
            initialError = initialError,
            initialSelectedUser = initialSelectedUser
        )
    }
}

/**
 * A helper structure to hold internal UI logic and stable callbacks.
 */
@Stable
class PopularPhotosInternalCallbacks internal constructor(
    val callbacks: PopularPhotosCallbacks
)

@Composable
fun rememberPopularPhotosInternalCallbacks(
    contentUiState: PopularPhotoContentUiState,
    screenCallbacks: PopularPhotosScreenCallbacks
): PopularPhotosInternalCallbacks {
    val currentCallbacks by rememberUpdatedState(screenCallbacks)

    return remember(contentUiState) {
        val onLoadResultStable: (PagingResult) -> Unit = { result ->
            if (result is PagingResult.Success || result is PagingResult.Error) {
                contentUiState.visible = result is PagingResult.Success
            }
            if (result is PagingResult.Error) {
                contentUiState.error = result.error
            }
        }

        PopularPhotosInternalCallbacks(
            callbacks = PopularPhotosCallbacks(
                isUserFollowed = { currentCallbacks.isUserFollowed(it) },
                isPhotoBookmarked = { currentCallbacks.isPhotoBookmarked(it) },
                onToggleFollow = { currentCallbacks.onToggleFollow(it) },
                onShowUserInfo = { contentUiState.selectedUser = it },
                onItemClicked = { id, index -> currentCallbacks.onItemClicked(id, index) },
                onViewPhotos = { name, first, last, user -> currentCallbacks.onViewPhotos(name, first, last, user) },
                onLoadResult = onLoadResultStable,
                onLoadedPhotos = { contentUiState.loadedPhotos = it }
            )
        )
    }
}
