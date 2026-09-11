package com.goforer.phogal.presentation.stateholder.uistate.home.following

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
import kotlinx.coroutines.launch

@Stable
data class FollowingUserActions(
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onOpenWebView: (firstName: String, url: String?) -> Unit,
    val onFollow: (User) -> Unit,
    val onLoadResult: (PagingResult) -> Unit
)

@Stable
data class FollowingUserScreenActions(
    val onBackPressed: () -> Unit,
    val onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    val onOpenWebView: (firstName: String, url: String) -> Unit,
    val onStart: () -> Unit = {},
    val onStop: () -> Unit = {}
)

@Stable
class FollowingUserContentUiState internal constructor(
    val followViewModel: FollowViewModel,
    val baseUiState: BaseUiState,
    val users: LazyPagingItems<User>,

    private val _enabledLoadPhotos: MutableState<Boolean>,
    private val _error: MutableState<ErrorEntity?>,
    private val _selectedUser: MutableState<User?>,
) {
    val enabledLoadPhotos: Boolean get() = _enabledLoadPhotos.value
    val error: ErrorEntity? get() = _error.value
    var selectedUser: User?
        get() = _selectedUser.value
        set(value) { _selectedUser.value = value }

    fun setEnabledLoadPhotos(enabledLoadPhotos: Boolean) {
        _enabledLoadPhotos.value = enabledLoadPhotos
    }

    fun setError(error: ErrorEntity?) {
        _error.value = error
    }
}

@Composable
fun rememberFollowingUserContentUiState(
    followViewModel: FollowViewModel,
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
            restore = { mutableStateOf(it?.toUser()) }
        )
    ) {
        mutableStateOf(null)
    }
): FollowingUserContentUiState {
    val users = followViewModel.followedUsers.collectAsLazyPagingItems()

    return remember(baseUiState, followViewModel, enabledLoadPhotos, error, selectedUser) {
        FollowingUserContentUiState(
            followViewModel = followViewModel,
            baseUiState = baseUiState,
            users = users,
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
class FollowingUserInternalActions internal constructor(
    val actions: FollowingUserActions
)

@Composable
fun rememberFollowingUserInternalActions(
    contentUiState: FollowingUserContentUiState,
    screenActions: FollowingUserScreenActions,
    snackbarHostState: SnackbarHostState
): FollowingUserInternalActions {
    val currentActions by rememberUpdatedState(screenActions)
    val text = R.string.user_info_has_no_portfolio

    return remember(contentUiState, snackbarHostState, text) {
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

        val onOpenWebViewLocal = { firstName: String, url: String? ->
            url.isNull({
                contentUiState.baseUiState.scope.launch {
                    snackbarHostState.showSnackbar("${firstName} ${contentUiState.baseUiState.context.getString(text)}")
                }
            }, {
                currentActions.onOpenWebView(firstName, it)
            })
        }

        FollowingUserInternalActions(
            actions = FollowingUserActions(
                onViewPhotos = { name, first, last, user -> currentActions.onViewPhotos(name, first, last, user) },
                onOpenWebView = onOpenWebViewLocal,
                onFollow = { contentUiState.followViewModel.setUserFollow(it) },
                onLoadResult = onLoadResultStable
            )
        )
    }
}
