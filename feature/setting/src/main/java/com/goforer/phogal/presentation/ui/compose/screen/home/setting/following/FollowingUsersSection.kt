package com.goforer.phogal.presentation.ui.compose.screen.home.setting.following

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import com.goforer.designsystem.component.EmptyStatePlaceholder
import com.goforer.designsystem.component.paging.PagingLoadStateCallbacks
import com.goforer.designsystem.component.paging.PagingLoadStateEffect
import com.goforer.designsystem.component.paging.PagingRenderCallbacks
import com.goforer.designsystem.component.paging.PagingRenderState
import com.goforer.designsystem.component.paging.contentItems
import com.goforer.designsystem.component.paging.rememberIsScrolledPastThreshold
import com.goforer.designsystem.component.paging.renderPagingLoadState
import com.goforer.designsystem.theme.Blue15
import com.goforer.designsystem.theme.Blue95
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.presentation.stateholder.uistate.home.following.FollowingUserCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.following.FollowingUserItemCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.following.FollowingUserSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.following.rememberFollowingUserItemUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.LoadingPicture
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import timber.log.Timber

@Composable
fun FollowingUsersSection(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    sectionUiState: FollowingUserSectionUiState,
    callbacks: FollowingUserCallbacks
) {
    // Uncomment the code below to improve the Following feature using Room.
    /*
    val isRefreshing by remember(users.loadState.refresh, manualRefreshing, sectionUiState.loadingDone) {
        derivedStateOf {
            manualRefreshing || (sectionUiState.loadingDone && users.itemCount > 0 && users.loadState.refresh is LoadState.Loading)
        }
    }
     */

    PagingLoadStateEffect(
        pagingItems = sectionUiState.users,
        callbacks = remember(sectionUiState, callbacks) {
            PagingLoadStateCallbacks(
                onLoadingStarted = { sectionUiState.loadingDone = false },
                onLoadingDone = { sectionUiState.loadingDone = true },
                onLoadResult = callbacks.onLoadResult,
                onRefreshTransition = { sectionUiState.manualRefreshing = it },
                onPaginationReached = { Timber.d("Loaded all photos") }
            )
        },
        logTag = "FollowingUsersSection"
    )

    // derivedStateOf: only triggers recomposition when the boolean actually flips,
    // not on every scroll tick.
    val isScrolledPastThreshold = sectionUiState.lazyListState.rememberIsScrolledPastThreshold()

    PullToRefreshBox(
        modifier = modifier.clip(RoundedCornerShape(2.dp)),
        isRefreshing = false, //isRefreshing :Uncomment the code below to improve the Following feature using Room.
        onRefresh = {
            sectionUiState.manualRefreshing = true
            sectionUiState.users.refresh()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            val isInspectionMode = LocalInspectionMode.current
            val layoutDirection = LocalLayoutDirection.current
            val isDark = isSystemInDarkTheme()
            val skyBlueBackground = if (isDark)
                Blue15
            else
                Blue95

            val followingUserItemCallbacks = remember(callbacks) {
                FollowingUserItemCallbacks(
                    onViewPhotos = callbacks.onViewPhotos,
                    onOpenWebView = callbacks.onOpenWebView,
                    onFollow = { user -> callbacks.onFollow(user) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(skyBlueBackground),
                state = sectionUiState.lazyListState,
                contentPadding = PaddingValues(
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                    top = 0.dp,
                    end = paddingValues.calculateRightPadding(layoutDirection),
                    bottom = paddingValues.calculateBottomPadding() + 36.dp
                )
            ) {
                renderLoadState(
                    modifier = modifier,
                    sectionUiState = sectionUiState,
                    followingUserItemCallbacks = followingUserItemCallbacks,
                    isInspectionMode = isInspectionMode
                )
            }

            ShowUpButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 4.dp,
                        bottom = paddingValues.calculateBottomPadding() + 18.dp
                    ),
                visible = isScrolledPastThreshold,
                onClick = {
                    sectionUiState.scrollToTop()
                    sectionUiState.visibleUpButton = false
                }
            )
        }
    }
}

/**
 * Dispatches the current [LoadState] of [FollowingUserSectionUiState.users] into the appropriate sub-renderer.
 * Kept as a LazyListScope extension so each sub-renderer can emit `item {}` / `items {}`
 * directly without re-wrapping.
 */
private fun LazyListScope.renderLoadState(
    modifier: Modifier = Modifier,
    sectionUiState: FollowingUserSectionUiState,
    followingUserItemCallbacks: FollowingUserItemCallbacks,
    isInspectionMode: Boolean
) {
    val isInitiallyLoading = sectionUiState.users.loadState.refresh is LoadState.Loading
    val suppressAnimation = isInitiallyLoading || isInspectionMode || sectionUiState.isResettingScroll

    renderPagingLoadState(
        state = PagingRenderState(
            items = sectionUiState.users,
            loadingDone = sectionUiState.loadingDone
        ),
        modifier = modifier,
        content = {
            contentItems(
                items = sectionUiState.users,
                key = { index, user -> "${user.id}_$index" },
                content = { padding, index, user ->
                    FollowingUsersItem(
                        modifier = Modifier
                            .padding(top = padding)
                            .then(
                                if (suppressAnimation) Modifier else Modifier.animateItem(tween(durationMillis = 250))
                            ),
                        followingUserItemUiState = rememberFollowingUserItemUiState(
                            userData = user.toString(),
                            index = index,
                            initialVisibleViewButton = true,
                            initialFollowed = true
                        ),
                        callbacks = followingUserItemCallbacks
                    )
                }
            )
        },
        callbacks = PagingRenderCallbacks(
            loadingPlaceholder = {
                items(5) {
                    LoadingUser(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                    )
                }
            },
            appendLoading = {
                item { LoadingPicture() }
            },
            emptyState = {
                item {
                    EmptyStatePlaceholder(
                        text = stringResource(id = R.string.setting_no_following)
                    )
                }
            }
        ),
    )
}
