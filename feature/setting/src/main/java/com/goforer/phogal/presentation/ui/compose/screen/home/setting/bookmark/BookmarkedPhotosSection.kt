package com.goforer.phogal.presentation.ui.compose.screen.home.setting.bookmark

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
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import com.goforer.designsystem.component.paging.PagingLoadStateCallbacks
import com.goforer.designsystem.component.paging.PagingLoadStateEffect
import com.goforer.designsystem.component.paging.PagingRenderCallbacks
import com.goforer.designsystem.component.paging.PagingRenderState
import com.goforer.designsystem.component.paging.contentItems
import com.goforer.designsystem.component.paging.rememberIsScrolledPastThreshold
import com.goforer.designsystem.component.paging.renderPagingLoadState
import com.goforer.designsystem.theme.Blue15
import com.goforer.designsystem.theme.Blue95
import com.goforer.phogal.presentation.stateholder.uistate.home.bookmark.BookmarkCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.bookmark.BookmarkSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.PictureItemCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPictureItemUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.LoadingPicture
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import timber.log.Timber

@Composable
fun BookmarkedPhotosSection(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    sectionUiState: BookmarkSectionUiState,
    callbacks: BookmarkCallbacks
) {
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
    val isScrolledPastThreshold = sectionUiState.lazyListState.rememberIsScrolledPastThreshold()

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

            val pictureItemCallbacks = remember(callbacks) {
                PictureItemCallbacks(
                    onFollowClick = callbacks.onToggleFollow,
                    onShowUserInfo = callbacks.onShowUserInfo,
                    onItemClicked = callbacks.onItemClicked,
                    onViewPhotos = callbacks.onViewPhotos
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
                    bottom = paddingValues.calculateBottomPadding() + 64.dp
                )
            ) {
                renderLoadState(
                    modifier = modifier,
                    sectionUiState = sectionUiState,
                    callbacks = callbacks,
                    pictureItemCallbacks = pictureItemCallbacks,
                    isInspectionMode = isInspectionMode
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
                    sectionUiState.scrollToTop()
                }
            )
        }
    }
}

/**
 * Dispatches the current [LoadState] of [BookmarkSectionUiState.photos] into the appropriate sub-renderer.
 * Kept as a LazyListScope extension so each sub-renderer can emit `item {}` / `items {}`
 * directly without re-wrapping.
 */
private fun LazyListScope.renderLoadState(
    modifier: Modifier = Modifier,
    sectionUiState: BookmarkSectionUiState,
    callbacks: BookmarkCallbacks,
    pictureItemCallbacks: PictureItemCallbacks,
    isInspectionMode: Boolean
) {
    val isInitiallyLoading = sectionUiState.photos.loadState.refresh is LoadState.Loading
    val suppressAnimation = isInitiallyLoading || isInspectionMode || sectionUiState.isResettingScroll

    renderPagingLoadState(
        state = PagingRenderState(
            items = sectionUiState.photos,
            loadingDone = sectionUiState.loadingDone
        ),
        modifier = modifier,
        content = {
            contentItems(
                items = sectionUiState.photos,
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
                        callbacks = pictureItemCallbacks
                    )
                }
            )
        },
        callbacks = PagingRenderCallbacks(
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
        ),
    )
}
