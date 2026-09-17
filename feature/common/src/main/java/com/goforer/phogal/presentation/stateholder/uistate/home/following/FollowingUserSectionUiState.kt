package com.goforer.phogal.presentation.stateholder.uistate.home.following

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.paging.compose.LazyPagingItems
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.uistate.home.common.base.BaseSectionUiState
import kotlinx.coroutines.CoroutineScope

@Stable
class FollowingUserSectionUiState internal constructor(
    val users: LazyPagingItems<User>,
    override val scope: CoroutineScope,
    override val lazyListState: LazyListState,
) : BaseSectionUiState(
    scope = scope,
    lazyListState = lazyListState,
)

@Composable
fun rememberFollowingUserSectionUiState(
    users: LazyPagingItems<User>,
    scope: CoroutineScope = rememberCoroutineScope(),
    lazyListState: LazyListState = LazyListState(),
): FollowingUserSectionUiState = remember(users, scope, lazyListState) {
    FollowingUserSectionUiState(
        users = users,
        scope = scope,
        lazyListState = lazyListState,
    )
}
