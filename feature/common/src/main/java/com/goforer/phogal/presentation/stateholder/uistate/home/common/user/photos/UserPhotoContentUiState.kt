package com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos

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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.base.extension.toUser
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.business.home.common.user.UserPhotosViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.ErrorEntity
import com.goforer.phogal.presentation.stateholder.uistate.PagingResult
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.base.BasePhotoContentUiState

@Stable
data class UserPhotosCallbacks(
    val isUserFollowed: (User) -> Boolean,
    val isPhotoBookmarked: (String) -> Boolean,
    val onToggleFollow: (User) -> Unit,
    val onShowUserInfo: (User) -> Unit,
    val onItemClicked: (id: String) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onLoadResult: (PagingResult) -> Unit
)

@Stable
data class UserPhotosScreenCallbacks(
    val isUserFollowed: (User) -> Boolean,
    val isPhotoBookmarked: (String) -> Boolean,
    val onToggleFollow: (User) -> Unit,
    val onItemClicked: (id: String) -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit = { _, _, _, _ -> },
    val onOpenWebView: (firstName: String, url: String) -> Unit,
    val onBackPressed: () -> Unit,
    val onStart: () -> Unit = {},
    val onStop: () -> Unit = {}
)

@Stable
class UserPhotoContentUiState internal constructor(
    override val baseUiState: BaseUiState,
    val userPhotosViewModel: UserPhotosViewModel,
    val photos: LazyPagingItems<Photo>,
    val name: String,
    val firstName: String,
    initialVisible: Boolean,
    initialError: ErrorEntity?,
    initialSelectedUser: User?,
) : BasePhotoContentUiState(
    baseUiState = baseUiState,
    initialVisible = initialVisible,
    initialError = initialError,
    initialSelectedUser = initialSelectedUser,
) {
    companion object {
        fun Saver(
            baseUiState: BaseUiState,
            userPhotosViewModel: UserPhotosViewModel,
            photos: LazyPagingItems<Photo>
        ): Saver<UserPhotoContentUiState, *> = Saver(
            save = {
                listOf(
                    it.name,
                    it.firstName,
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
                val name = it[0] as String
                val firstName = it[1] as String
                val visible = it[2] as Boolean
                val errorMap = it[3] as? Map<*, *>
                val selectedUserStr = it[4] as? String
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
                UserPhotoContentUiState(
                    baseUiState = baseUiState,
                    userPhotosViewModel = userPhotosViewModel,
                    photos = photos,
                    name = name,
                    firstName = firstName,
                    initialVisible = visible,
                    initialError = error,
                    initialSelectedUser = selectedUserStr?.toUser()
                )
            }
        )
    }
}

@Composable
fun rememberUserPhotosContentUiState(
    userPhotosViewModel: UserPhotosViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    name: String = "",
    firstName: String = "",
    initialVisible: Boolean = false,
    initialError: ErrorEntity? = null,
    initialSelectedUser: User? = null
): UserPhotoContentUiState  {
    val photos = userPhotosViewModel.photos.collectAsLazyPagingItems()

    return rememberSaveable(
        baseUiState, userPhotosViewModel, photos,
        saver = UserPhotoContentUiState.Saver(baseUiState, userPhotosViewModel, photos)
    ) {
        UserPhotoContentUiState(
            baseUiState = baseUiState,
            userPhotosViewModel = userPhotosViewModel,
            photos = photos,
            name = name,
            firstName = firstName,
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
class UserPhotosInternalCallbacks internal constructor(
    val callbacks: UserPhotosCallbacks
)

@Composable
fun rememberUserPhotosInternalCallbacks(
    contentUiState: UserPhotoContentUiState,
    screenCallbacks: UserPhotosScreenCallbacks,
    snackbarHostState: SnackbarHostState
): UserPhotosInternalCallbacks {
    val currentCallbacks by rememberUpdatedState(screenCallbacks)

    return remember(contentUiState, snackbarHostState) {
        val onLoadResultStable: (PagingResult) -> Unit = { result ->
            if (result is PagingResult.Success || result is PagingResult.Error) {
                contentUiState.visible = result is PagingResult.Success
            }
            if (result is PagingResult.Error) {
                contentUiState.error = result.error
            }
        }

        UserPhotosInternalCallbacks(
            callbacks = UserPhotosCallbacks(
                isUserFollowed = { currentCallbacks.isUserFollowed(it) },
                isPhotoBookmarked = { currentCallbacks.isPhotoBookmarked(it) },
                onToggleFollow = { currentCallbacks.onToggleFollow(it) },
                onShowUserInfo = { contentUiState.selectedUser = it },
                onItemClicked = { currentCallbacks.onItemClicked(it) },
                onViewPhotos = { _, _, _, _ -> },
                onLoadResult = onLoadResultStable
            )
        )
    }
}