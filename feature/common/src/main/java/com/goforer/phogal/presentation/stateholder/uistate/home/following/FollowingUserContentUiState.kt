package com.goforer.phogal.presentation.stateholder.uistate.home.following

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
import com.goforer.base.extension.isNull
import com.goforer.base.extension.toUser
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.business.home.setting.follow.FollowViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.ErrorEntity
import com.goforer.phogal.presentation.stateholder.uistate.PagingResult
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.BaseContentUiState
import kotlinx.coroutines.launch

@Stable
data class FollowingUserCallbacks(
    // Action
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onOpenWebView: (firstName: String, url: String?) -> Unit,
    val onFollow: (User) -> Unit,
    val onLoadResult: (PagingResult) -> Unit
)

@Stable
data class FollowingUserScreenCallbacks(
    val onBackPressed: () -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onOpenWebView: (firstName: String, url: String) -> Unit,
    val onStart: () -> Unit = {},
    val onStop: () -> Unit = {}
)

@Stable
class FollowingUserContentUiState internal constructor(
    override val baseUiState: BaseUiState,
    val followViewModel: FollowViewModel,
    val users: LazyPagingItems<User>,
    initialEnabledLoadPhotos: Boolean,
    initialError: ErrorEntity?,
    initialSelectedUser: User?,
) : BaseContentUiState(
    baseUiState = baseUiState,
    initialError = initialError,
    initialSelectedUser = initialSelectedUser,
) {
    var enabledLoadPhotos: Boolean by mutableStateOf(initialEnabledLoadPhotos)

    companion object {
        fun Saver(
            followViewModel: FollowViewModel,
            users: LazyPagingItems<User>,
            baseUiState: BaseUiState
        ): Saver<FollowingUserContentUiState, *> = Saver(
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
                FollowingUserContentUiState(
                    followViewModel = followViewModel,
                    users = users,
                    initialEnabledLoadPhotos = enabledLoadPhotos,
                    baseUiState = baseUiState,
                    initialError = error,
                    initialSelectedUser = selectedUserStr?.toUser()
                )
            }
        )
    }
}

@Composable
fun rememberFollowingUserContentUiState(
    followViewModel: FollowViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    initialEnabledLoadPhotos: Boolean = true,
    initialError: ErrorEntity? = null,
    initialSelectedUser: User? = null
): FollowingUserContentUiState {
    val users = followViewModel.followedUsers.collectAsLazyPagingItems()

    return rememberSaveable(
        saver = FollowingUserContentUiState.Saver(followViewModel, users, baseUiState)
    ) {
        FollowingUserContentUiState(
            followViewModel = followViewModel,
            baseUiState = baseUiState,
            users = users,
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
class FollowingUserInternalCallbacks internal constructor(
    val callbacks: FollowingUserCallbacks
)

@Composable
fun rememberFollowingUserInternalCallbacks(
    contentUiState: FollowingUserContentUiState,
    screenCallbacks: FollowingUserScreenCallbacks,
    snackbarHostState: SnackbarHostState
): FollowingUserInternalCallbacks {
    val currentCallbacks by rememberUpdatedState(screenCallbacks)
    val text = R.string.user_info_has_no_portfolio

    return remember(contentUiState, snackbarHostState, text) {
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

        val onOpenWebViewLocal = { firstName: String, url: String? ->
            url.isNull({
                contentUiState.baseUiState.scope.launch {
                    snackbarHostState.showSnackbar("${firstName} ${contentUiState.baseUiState.context.getString(text)}")
                }
            }, {
                currentCallbacks.onOpenWebView(firstName, it)
            })
        }

        FollowingUserInternalCallbacks(
            callbacks = FollowingUserCallbacks(
                onViewPhotos = { name, first, last, user -> currentCallbacks.onViewPhotos(name, first, last, user) },
                onOpenWebView = onOpenWebViewLocal,
                onFollow = { contentUiState.followViewModel.setUserFollow(it) },
                onLoadResult = onLoadResultStable
            )
        )
    }
}
