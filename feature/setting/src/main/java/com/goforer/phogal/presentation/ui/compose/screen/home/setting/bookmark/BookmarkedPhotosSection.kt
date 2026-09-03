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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.goforer.designsystem.component.paging.PagingLoadStateEffect
import com.goforer.designsystem.component.paging.rememberLazyListState
import com.goforer.designsystem.component.paging.renderPagingLoadState
import com.goforer.designsystem.component.paging.contentItems
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.uistate.UIConstants.SCROLL_OFFSET_SIGNAL
import com.goforer.phogal.presentation.stateholder.uistate.UIConstants.UP_BUTTON_THRESHOLD
import com.goforer.phogal.presentation.stateholder.uistate.home.setting.bookmark.BookmarkSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.setting.bookmark.rememberBookmarkSectionUiState
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.business.home.setting.follow.FollowViewModel
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPictureItemUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.LoadingPicture
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import com.goforer.designsystem.theme.Blue15
import com.goforer.designsystem.theme.Blue95
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkedPhotosSection(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    sectionUiState: BookmarkSectionUiState = rememberBookmarkSectionUiState(),
    photos: LazyPagingItems<Picture>,
    followViewModel: FollowViewModel = hiltViewModel(),
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (item: Picture, index: Int) -> Unit,
    onLoadResult: (isSuccessful: Boolean, message: String) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit
) {
    val lazyListState = photos.rememberLazyListState()
    val scope = rememberCoroutineScope()
    var manualRefreshing by remember { mutableStateOf(false) }
    // Uncomment the code below to improve the Following feature using Room.
    /*
    val isRefreshing by remember(photos.loadState.refresh, manualRefreshing, sectionUiState.loadingDone) {
        derivedStateOf {
            manualRefreshing || (sectionUiState.loadingDone && photos.itemCount > 0 && photos.loadState.refresh is LoadState.Loading)
        }
    }
    
     */

    PagingLoadStateEffect(
        pagingItems = photos,
        onLoadingStarted = { sectionUiState.setLoadingStarted() },
        onLoadingDone = { sectionUiState.setLoadingDone() },
        onLoadResult = onLoadResult,
        onRefreshTransition = { manualRefreshing = it },
        onPaginationReached = { Timber.d("Loaded all photos") },
        logTag = "BookmarkedPhotosSection"
    )

    // derivedStateOf: only triggers recomposition when the boolean actually flips,
    // not on every scroll tick.
    val isScrolledPastThreshold by remember(lazyListState) {
        derivedStateOf {
            !lazyListState.isScrollInProgress && lazyListState.firstVisibleItemIndex > UP_BUTTON_THRESHOLD &&
                    lazyListState.firstVisibleItemScrollOffset > SCROLL_OFFSET_SIGNAL
        }
    }

    PullToRefreshBox(
        modifier = modifier.clip(RoundedCornerShape(2.dp)),
        isRefreshing = false, //isRefreshing :Uncomment the code below to improve the Following feature using Room.
        onRefresh = {
            manualRefreshing = true
            photos.refresh()
        }
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(0.2.dp))
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
                    top = 0.dp,
                    end = paddingValues.calculateRightPadding(layoutDirection) ,
                    bottom = paddingValues.calculateBottomPadding() + 64.dp
                )
            ) {
                renderLoadState(
                    photos = photos,
                    sectionUiState = sectionUiState,
                    followViewModel = followViewModel,
                    onShowUserInfo = onShowUserInfo,
                    onItemClicked = onItemClicked,
                    onViewPhotos = onViewPhotos
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
                    scope.launch {
                        lazyListState.animateScrollToItem (0)
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
    followViewModel: FollowViewModel,
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (item: Picture, index: Int) -> Unit,
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
                    Timber.d("Photo Index is : $index")
                    PictureItem(
                        modifier = Modifier
                            .padding(top = padding)
                            .animateItem(tween(durationMillis = 250)),
                        pictureItemUiState = rememberPictureItemUiState(
                            picture = rememberSaveable { mutableStateOf(photo) }
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
