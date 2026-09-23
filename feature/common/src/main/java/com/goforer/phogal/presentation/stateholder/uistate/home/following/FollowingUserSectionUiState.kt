package com.goforer.phogal.presentation.stateholder.uistate.home.following

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.paging.compose.LazyPagingItems
import com.goforer.designsystem.component.paging.rememberLazyListState
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.uistate.home.common.base.BaseSectionUiState
import kotlinx.coroutines.CoroutineScope

@Stable
class FollowingUserSectionUiState internal constructor(
    users: LazyPagingItems<User>,
    override val scope: CoroutineScope,
    override val lazyListState: LazyListState,
) : BaseSectionUiState(
    scope = scope,
    lazyListState = lazyListState,
) {
    var users: LazyPagingItems<User> by mutableStateOf(users)
}

@Composable
fun rememberFollowingUserSectionUiState(
    users: LazyPagingItems<User>,
    scope: CoroutineScope = rememberCoroutineScope(),
    lazyListState: LazyListState = users.rememberLazyListState(),
): FollowingUserSectionUiState {
    val uiState = remember {
        FollowingUserSectionUiState(
            users = users,
            scope = scope,
            lazyListState = lazyListState,
        )
    }
    uiState.users = users
    return uiState
}
