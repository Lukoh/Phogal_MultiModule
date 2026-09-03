package com.goforer.phogal.presentation.ui.compose.screen.home.gallery

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.goforer.designsystem.component.paging.PagingLoadStateEffect
import com.goforer.designsystem.component.paging.contentItems
import com.goforer.designsystem.component.paging.rememberLazyListState
import com.goforer.designsystem.component.paging.renderPagingLoadState
import com.goforer.designsystem.theme.Blue15
import com.goforer.designsystem.theme.Blue95
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.stateholder.business.home.setting.follow.FollowViewModel
import com.goforer.phogal.presentation.stateholder.uistate.UIConstants.SCROLL_OFFSET_SIGNAL
import com.goforer.phogal.presentation.stateholder.uistate.UIConstants.UP_BUTTON_THRESHOLD
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPhotoItemUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotosSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchPhotosSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.LoadingPicture
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.PhotoItem
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchPhotosSection(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    photos: LazyPagingItems<Photo>,
    sectionUiState: SearchPhotosSectionUiState = rememberSearchPhotosSectionUiState(
        rememberCoroutineScope(),
        rememberSaveable { mutableStateOf(false) }
    ),
    bookmarkViewModel: BookmarkViewModel = hiltViewModel(),
    followViewModel: FollowViewModel = hiltViewModel(),
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (item: Photo, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onLoadResult: (isSuccessful: Boolean, message: String) -> Unit,
    onScroll: (isScrolling: Boolean) -> Unit
) {
    val lazyListState = photos.rememberLazyListState()
    var manualRefreshing by remember { mutableStateOf(false) }
    val isRefreshing by remember(photos.loadState.refresh, manualRefreshing, sectionUiState.loadingDone) {
        derivedStateOf {
            manualRefreshing || (sectionUiState.loadingDone && photos.itemCount > 0 && photos.loadState.refresh is LoadState.Loading)
        }
    }

    PagingLoadStateEffect(
        pagingItems = photos,
        onLoadingStarted = { sectionUiState.setLoadingStarted() },
        onLoadingDone = { sectionUiState.setLoadingDone() },
        onLoadResult = { isSuccessful, message -> onLoadResult(isSuccessful, message) },
        onRefreshTransition = { manualRefreshing = it },
        onPaginationReached = { Timber.d("Loaded all photos") },
        logTag = "SearchPhotosSection"
    )

    // derivedStateOf: only triggers recomposition when the boolean actually flips,
    // not on every scroll tick.
    val isScrolledPastThreshold by remember(lazyListState) {
        derivedStateOf {
            !lazyListState.isScrollInProgress && lazyListState.firstVisibleItemIndex > UP_BUTTON_THRESHOLD &&
                    lazyListState.firstVisibleItemScrollOffset > SCROLL_OFFSET_SIGNAL
        }
    }

    // Propagate scroll signal to parent — only when isScrollInProgress changes,
    // not on every pixel of scrolling.
    LaunchedEffect(lazyListState) {
        snapshotFlowScrollState(lazyListState).collect { scrolling ->
            onScroll(scrolling)
        }
    }

    // Nav3-stable Material 3 PullToRefreshBox replaces the deprecated
    // androidx.compose.material.pullrefresh.* APIs. The container handles the
    // refresh indicator itself — no separate PullRefreshIndicator needed.
    PullToRefreshBox(
        modifier = modifier.clip(RoundedCornerShape(2.dp)),
        isRefreshing = isRefreshing,
        onRefresh = {
            manualRefreshing = true
            photos.refresh()
        }
    ) {
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
            state = lazyListState,
            contentPadding = PaddingValues(
                start = paddingValues.calculateLeftPadding(layoutDirection),
                top = paddingValues.calculateLeftPadding(layoutDirection),
                end = paddingValues.calculateRightPadding(layoutDirection) ,
                bottom = paddingValues.calculateBottomPadding() + 46.dp
            )
        ) {
            renderLoadState(
                photos = photos,
                sectionUiState = sectionUiState,
                bookmarkViewModel = bookmarkViewModel,
                followViewModel = followViewModel,
                onShowUserInfo = onShowUserInfo,
                onItemClicked = onItemClicked,
                onViewPhotos = onViewPhotos
            )
        }

        // Show up-button only when user has scrolled past the threshold and isn't
        // actively scrolling (prevents the button from flickering during drags).
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
                    lazyListState.animateScrollToItem(0)
                }
            }
        )
    }
}

/**
 * Dispatches the current [LoadState] of [photos] into the appropriate sub-renderer.
 * Kept as a LazyListScope extension so each sub-renderer can emit `item {}` / `items {}`
 * directly without re-wrapping.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.renderLoadState(
    photos: LazyPagingItems<Photo>,
    sectionUiState: SearchPhotosSectionUiState,
    bookmarkViewModel: BookmarkViewModel,
    followViewModel: FollowViewModel,
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (item: Photo, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit
) {
    renderPagingLoadState(
        items = photos,
        loadingDone = sectionUiState.loadingDone,
        content = {
            contentItems(
                items = photos,
                key = { index, photo -> "${photo.id}_$index" },
                content = { padding, index, photo ->
                    PhotoItem(
                        modifier = Modifier
                            .padding(top = padding)
                            .animateItem(tween(durationMillis = 250)),
                        state = rememberPhotoItemUiState(
                            index = rememberSaveable { mutableIntStateOf(index) },
                            photo = rememberSaveable { mutableStateOf(photo) },
                            visibleViewButton = rememberSaveable { mutableStateOf(true) },
                            bookmarked = rememberSaveable {
                                mutableStateOf(bookmarkViewModel.isPhotoBookmarked(photo.id))
                            }
                        ),
                        followViewModel = followViewModel,
                        onShowUserInfo = onShowUserInfo,
                        onItemClicked = onItemClicked,
                        onViewPhotos = onViewPhotos
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

/**
 * Converts a LazyListState into a Flow<Boolean> that emits whenever the
 * `isScrollInProgress` value changes. Uses `snapshotFlow` so Compose's snapshot
 * system — not polling — drives the emissions.
 */
private fun snapshotFlowScrollState(state: LazyListState) =
    snapshotFlow { state.isScrollInProgress }
        .distinctUntilChanged()
