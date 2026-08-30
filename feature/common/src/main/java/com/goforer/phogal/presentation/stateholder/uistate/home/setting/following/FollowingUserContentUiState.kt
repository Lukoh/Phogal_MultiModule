package com.goforer.phogal.presentation.stateholder.uistate.home.setting.following

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.business.home.setting.follow.FollowViewModel
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
class FollowingUserContentUiState internal constructor(
    val followViewModel: FollowViewModel,
    val baseUiState: BaseUiState,
    val users: LazyPagingItems<User>,

    private val _enabledLoadPhotos: MutableState<Boolean>
) {
    val enabledLoadPhotos: Boolean get() = _enabledLoadPhotos.value

    fun setEnabledLoadPhotos(enabledLoadPhotos: Boolean) {
        _enabledLoadPhotos.value = enabledLoadPhotos
    }
}

@Composable
fun rememberFollowingUserContentUiState(
    followViewModel: FollowViewModel,
    baseUiState: BaseUiState = rememberBaseUiState(),
    enabledLoadPhotos: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) }
): FollowingUserContentUiState {
    val users = followViewModel.followedUsers.collectAsLazyPagingItems()

    return remember(baseUiState, followViewModel, enabledLoadPhotos) {
        FollowingUserContentUiState(
            followViewModel = followViewModel,
            baseUiState = baseUiState,
            users = users,
            _enabledLoadPhotos = enabledLoadPhotos
        )
    }
}


