package com.goforer.phogal.presentation.ui.compose.screen.home.setting.following

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.goforer.designsystem.component.EmptyStatePlaceholder
import com.goforer.designsystem.component.paging.PagingLoadStateCallbacks
import com.goforer.designsystem.component.paging.PagingLoadStateEffect
import com.goforer.designsystem.component.paging.contentItems
import com.goforer.designsystem.component.paging.rememberLazyListState
import com.goforer.designsystem.component.paging.renderPagingLoadState
import com.goforer.designsystem.theme.Blue15
import com.goforer.designsystem.theme.Blue95
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.uistate.UIConstants.SCROLL_OFFSET_SIGNAL
import com.goforer.phogal.presentation.stateholder.uistate.UIConstants.UP_BUTTON_THRESHOLD
import com.goforer.phogal.presentation.stateholder.uistate.home.following.FollowingUserCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.following.FollowingUserItemCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.following.FollowingUserSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.following.rememberFollowingUserItemUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.following.rememberFollowingUserSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.LoadingPicture
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FollowingUsersSection(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    sectionUiState: FollowingUserSectionUiState,
    callbacks: FollowingUserCallbacks
) {
    // When a refresh starts, mark that we should scroll to top once data arrives.
    LaunchedEffect(sectionUiState.users.loadState.refresh) {
        if (sectionUiState.users.loadState.refresh is LoadState.Loading) {
            sectionUiState.isResettingScroll = true
        }
    }

    // Reset scroll position to 0 safely only when initial page data load completes
    // successfully and we have items.
    LaunchedEffect(sectionUiState.users.loadState.refresh, sectionUiState.users.itemCount) {
        if (sectionUiState.isResettingScroll && sectionUiState.users.loadState.refresh is LoadState.NotLoading && sectionUiState.users.itemCount > 0) {
            sectionUiState.lazyListState.scrollToItem(0)
            sectionUiState.isResettingScroll = false
        }
    }

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
    val isScrolledPastThreshold by remember(sectionUiState.lazyListState) {
        derivedStateOf {
            !sectionUiState.lazyListState.isScrollInProgress && sectionUiState.lazyListState.firstVisibleItemIndex > UP_BUTTON_THRESHOLD &&
                    sectionUiState.lazyListState.firstVisibleItemScrollOffset > SCROLL_OFFSET_SIGNAL
        }
    }

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

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(skyBlueBackground),
                state = sectionUiState.lazyListState,
                contentPadding = PaddingValues(
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                    top = 0.dp,
                    end = paddingValues.calculateRightPadding(layoutDirection) ,
                    bottom = paddingValues.calculateBottomPadding() + 36.dp
                )
            ) {
                renderLoadState(
                    users = sectionUiState.users,
                    sectionUiState = sectionUiState,
                    callbacks = callbacks,
                    isInspectionMode = isInspectionMode,
                    isResettingScroll = sectionUiState.isResettingScroll
                )
            }
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
                sectionUiState.scope.launch {
                    sectionUiState.lazyListState.animateScrollToItem (0)
                    sectionUiState.visibleUpButton = false
                }
            }
        )
    }
}

/**
 * Dispatches the current [LoadState] of [users] into the appropriate sub-renderer.
 * Kept as a LazyListScope extension so each sub-renderer can emit `item {}` / `items {}`
 * directly without re-wrapping.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.renderLoadState(
    users: LazyPagingItems<User>,
    sectionUiState: FollowingUserSectionUiState,
    callbacks: FollowingUserCallbacks,
    isInspectionMode: Boolean,
    isResettingScroll: Boolean
) {
    val isInitiallyLoading = users.loadState.refresh is LoadState.Loading
    val suppressAnimation = isInitiallyLoading || isInspectionMode || isResettingScroll

    renderPagingLoadState(
        items = users,
        loadingDone = sectionUiState.loadingDone,
        content = {
            contentItems(
                items = users,
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
                        callbacks = remember(callbacks) {
                            FollowingUserItemCallbacks(
                                onViewPhotos = callbacks.onViewPhotos,
                                onOpenWebView = callbacks.onOpenWebView,
                                onFollow = { callbacks.onFollow(it) }
                            )
                        }
                    )
                }
            )
        },
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
    )
}
