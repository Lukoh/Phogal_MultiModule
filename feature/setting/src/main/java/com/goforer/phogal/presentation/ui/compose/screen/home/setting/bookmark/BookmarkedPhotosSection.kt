package com.goforer.phogal.presentation.ui.compose.screen.home.setting.bookmark

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.goforer.designsystem.component.paging.PagingLoadStateCallbacks
import com.goforer.designsystem.component.paging.PagingLoadStateEffect
import com.goforer.designsystem.component.paging.contentItems
import com.goforer.designsystem.component.paging.rememberLazyListState
import com.goforer.designsystem.component.paging.renderPagingLoadState
import com.goforer.designsystem.theme.Blue15
import com.goforer.designsystem.theme.Blue95
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.uistate.UIConstants.SCROLL_OFFSET_SIGNAL
import com.goforer.phogal.presentation.stateholder.uistate.UIConstants.UP_BUTTON_THRESHOLD
import com.goforer.phogal.presentation.stateholder.uistate.home.bookmark.BookmarkCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.bookmark.BookmarkSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.bookmark.rememberBookmarkSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.PictureItemCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPictureItemUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.LoadingPicture
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkedPhotosSection(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    sectionUiState: BookmarkSectionUiState,
    callbacks: BookmarkCallbacks
) {
    // When a refresh starts, mark that we should scroll to top once data arrives.
    LaunchedEffect(sectionUiState.photos.loadState.refresh) {
        if (sectionUiState.photos.loadState.refresh is LoadState.Loading) {
            sectionUiState.isResettingScroll = true
        }
    }

    // Reset scroll position to 0 safely only when initial page data load completes
    // successfully and we have items.
    LaunchedEffect(sectionUiState.photos.loadState.refresh, sectionUiState.photos.itemCount) {
        if (sectionUiState.isResettingScroll && sectionUiState.photos.loadState.refresh is LoadState.NotLoading && sectionUiState.photos.itemCount > 0) {
            sectionUiState.lazyListState.scrollToItem(0)
            sectionUiState.isResettingScroll = false
        }
    }

    PagingLoadStateEffect(
        pagingItems = sectionUiState.photos,
        callbacks = remember(sectionUiState, callbacks) {
            PagingLoadStateCallbacks(
                onLoadingStarted = { sectionUiState.loadingDone = false },
                onLoadingDone = { sectionUiState.loadingDone = true },
                onLoadResult = callbacks.onLoadResult,
                onRefreshTransition = { sectionUiState.manualRefreshing = it },
                onPaginationReached = { Timber.d("Loaded all photos") }
            )
        },
        logTag = "BookmarkedPhotosSection"
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
            sectionUiState.photos.refresh()
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
                    bottom = paddingValues.calculateBottomPadding() + 64.dp
                )
            ) {
                renderLoadState(
                    photos = sectionUiState.photos,
                    sectionUiState = sectionUiState,
                    callbacks = callbacks,
                    isInspectionMode = isInspectionMode,
                    isResettingScroll = sectionUiState.isResettingScroll
                )
            }

            ShowUpButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 4.dp,
                        bottom = paddingValues.calculateBottomPadding() - 18.dp
                    ),
                visible = isScrolledPastThreshold,
                onClick = {
                    sectionUiState.scope.launch {
                        sectionUiState.lazyListState.animateScrollToItem (0)
                    }
                }
            )
        }
    }
}

/**
 * Dispatches the current [LoadState] of [photos] into the appropriate sub-renderer.
 * Kept as a LazyListScope extension so each sub-renderer can emit `item {}` / `items {}`
 * directly without re-wrapping.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.renderLoadState(
    photos: LazyPagingItems<Picture>,
    sectionUiState: BookmarkSectionUiState,
    callbacks: BookmarkCallbacks,
    isInspectionMode: Boolean,
    isResettingScroll: Boolean
) {
    val isInitiallyLoading = photos.loadState.refresh is LoadState.Loading
    val suppressAnimation = isInitiallyLoading || isInspectionMode || isResettingScroll

    renderPagingLoadState(
        items = photos,
        loadingDone = sectionUiState.loadingDone,
        content = {
            contentItems(
                items = photos,
                key = { index, photo -> "${photo.id}_$index" },
                content = { padding, index, photo ->
                    Timber.d("Photo Index is : $index")
                    PictureItem(
                        modifier = Modifier
                            .padding(top = padding)
                            .then(
                                if (suppressAnimation) Modifier else Modifier.animateItem(tween(durationMillis = 250))
                            ),
                        pictureItemUiState = rememberPictureItemUiState(
                            picture = photo,
                            index = index
                        ),
                        isFollowed = callbacks.isUserFollowed(photo.user),
                        callbacks = remember(callbacks) {
                            PictureItemCallbacks(
                                onFollowClick = callbacks.onToggleFollow,
                                onShowUserInfo = callbacks.onShowUserInfo,
                                onItemClicked = callbacks.onItemClicked,
                                onViewPhotos = callbacks.onViewPhotos
                            )
                        }
                    )
                }
            )
        },
        loadingPlaceholder = {
            items(5) { index ->
                val padding = if (index == 0)
                    2.dp
                else
                    0.5.dp

                LoadingPicture(
                    modifier = Modifier
                        .padding(top = padding)
                        .fillMaxWidth(),
                    enableLoadIndicator = index == 0
                )
            }
        },
        appendLoading = {
            item { LoadingPicture() }
        }
    )
}
