package com.goforer.phogal.presentation.ui.compose.screen.home.setting.following

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.ui.compose.screen.home.common.InitScreen

@Composable
fun FollowingUsersContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    users: LazyPagingItems<User>,
    enabledLoadPhotos: Boolean,
    onLoadResult: (isSuccessful: Boolean, message: String) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onOpenWebView: (firstName: String, url: String?) -> Unit,
    onFollow: (userUiState: User) -> Unit
) {
    if (users.itemCount > 0) {
        FollowingUsersSection(
            modifier = modifier,
            paddingValues = paddingValues,
            users = users,
            onLoadResult = onLoadResult,
            onViewPhotos = onViewPhotos,
            onOpenWebView = onOpenWebView,
            onFollow = onFollow
        )
    } else {
        if (enabledLoadPhotos) {
            BoxWithConstraints(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val isTablet = maxWidth > 600.dp

                InitScreen(
                    modifier = Modifier.padding(horizontal = if (isTablet) 40.dp else 16.dp),
                    text = stringResource(id = R.string.setting_no_following)
                )
            }
        }
    }
}