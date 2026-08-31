@file:Suppress("UNCHECKED_CAST")

package com.goforer.phogal.presentation.ui.compose.screen.home.popularphotos

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import com.goforer.designsystem.component.state.rememberLazyListState
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.business.home.setting.bookmark.BookmarkViewModel
import com.goforer.phogal.presentation.stateholder.business.home.setting.follow.FollowViewModel
import com.goforer.phogal.presentation.stateholder.uistate.UIConstants.SCROLL_OFFSET_SIGNAL
import com.goforer.phogal.presentation.stateholder.uistate.UIConstants.UP_BUTTON_THRESHOLD
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPhotoItemUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.PopularPhotosSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.rememberPopularPhotosSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.LoadingPicture
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.PhotoItem
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import com.goforer.phogal.presentation.ui.compose.screen.home.common.EmptyState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.ErrorRow
import com.goforer.designsystem.theme.Blue15
import com.goforer.designsystem.theme.Blue95
import kotlinx.coroutines.launch
import timber.log.Timber

private const val PAGE_SIZE_HINT = 10

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PopularPhotosSection(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    photos: LazyPagingItems<Photo>,
    sectionUiState: PopularPhotosSectionUiState = rememberPopularPhotosSectionUiState(),
    bookmarkViewModel: BookmarkViewModel = hiltViewModel(),
    followViewModel: FollowViewModel = hiltViewModel(),
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (item: Photo, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onSuccess: (isSuccessful: Boolean) -> Unit,
    onLoadedPhotos: (isLoadedPhotos: Boolean) -> Unit
) {
    val lazyListState = photos.rememberLazyListState()
    var manualRefreshing by remember { mutableStateOf(false) }
    val isRefreshing by remember(photos.loadState.refresh, manualRefreshing) {
        derivedStateOf {
            manualRefreshing || photos.loadState.refresh is LoadState.Loading
        }
    }

    LaunchedEffect(photos.loadState.refresh) {
        if (photos.loadState.refresh !is LoadState.Loading) {
            manualRefreshing = false
        }
    }

    // derivedStateOf: only triggers recomposition when the boolean actually flips,
    // not on every scroll tick.
    val isScrolledPastThreshold by remember(lazyListState) {
        derivedStateOf {
            !lazyListState.isScrollInProgress && lazyListState.firstVisibleItemIndex > UP_BUTTON_THRESHOLD &&
                    lazyListState.firstVisibleItemScrollOffset > SCROLL_OFFSET_SIGNAL
        }
    }

    val layoutDirection = LocalLayoutDirection.current

    // Material 3 PullToRefreshBox — default indicator is rendered automatically.
    PullToRefreshBox(
        modifier = modifier.clip(RoundedCornerShape(2.dp)),
        isRefreshing = isRefreshing,
        onRefresh = {
            manualRefreshing = true
            photos.refresh()
        }
    ) {
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
                bookmarkViewModel = bookmarkViewModel,
                followViewModel = followViewModel,
                onShowUserInfo = onShowUserInfo,
                onItemClicked = onItemClicked,
                onViewPhotos = onViewPhotos,
                onSuccess = onSuccess,
                onLoadedPhotos = onLoadedPhotos
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
                    lazyListState.animateScrollToItem (0)
                }
            }
        )

        LaunchedEffect(photos) {
            sectionUiState.setLoadingStarted()
        }

        var hasStartedLoading by remember(photos) { mutableStateOf(false) }

        LaunchedEffect(photos.loadState.refresh) {
            when (photos.loadState.refresh) {
                is LoadState.Loading -> {
                    hasStartedLoading = true
                    sectionUiState.setLoadingStarted()
                }
                is LoadState.NotLoading -> {
                    if (photos.itemCount > 0 || (hasStartedLoading && photos.loadState.append.endOfPaginationReached)) {
                        sectionUiState.setLoadingDone()
                    }
                }
                else -> Unit
            }
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
    photos: LazyPagingItems<Photo>,
    sectionUiState: PopularPhotosSectionUiState,
    bookmarkViewModel: BookmarkViewModel,
    followViewModel: FollowViewModel,
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (item: Photo, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onSuccess: (Boolean) -> Unit,
    onLoadedPhotos: (isLoadedPhotos: Boolean) -> Unit
) {
    val loadState = photos.loadState

    if (photos.itemCount == 0) {
        when (loadState.refresh) {
            is LoadState.Loading -> {
                items(5) { index ->
                    LoadingPicture(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        enableLoadIndicator = index == 0
                    )
                }
            }
            is LoadState.NotLoading -> {
                if (sectionUiState.loadingDone) {
                    item { EmptyState() }
                } else {
                    items(5) { index ->
                        LoadingPicture(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            enableLoadIndicator = index == 0
                        )
                    }
                }
            }
            is LoadState.Error -> {
                onSuccess(false)
                val error = (loadState.refresh as LoadState.Error).error
                item { ErrorRow(throwable = error, onRetry = { photos.retry() }) }
            }
        }
    } else {
        photoItems(
            photos = photos,
            bookmarkViewModel = bookmarkViewModel,
            followViewModel = followViewModel,
            onShowUserInfo = onShowUserInfo,
            onItemClicked = onItemClicked,
            onViewPhotos = onViewPhotos,
            onLoadedPhotos = onLoadedPhotos
        )

        when (loadState.refresh) {
            is LoadState.NotLoading -> {
                onSuccess(true)
            }
            is LoadState.Error -> {
                onSuccess(false)
                val error = (loadState.refresh as LoadState.Error).error
                item { ErrorRow(throwable = error, onRetry = { photos.retry() }) }
            }
            is LoadState.Loading -> {
                onSuccess(true)
            }
        }
    }

    // Append (next-page) state is rendered independently from refresh state.
    when (loadState.append) {
        is LoadState.Loading -> {
            Timber.d("Pagination Loading")
        }
        is LoadState.Error -> {
            Timber.d("Pagination broken Error")
            onSuccess(false)
            val error = (loadState.append as LoadState.Error).error
            item { ErrorRow(throwable = error, onRetry = { photos.retry() }) }
        }
        else -> Unit
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.photoItems(
    photos: LazyPagingItems<Photo>,
    bookmarkViewModel: BookmarkViewModel,
    followViewModel: FollowViewModel,
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (item: Photo, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onLoadedPhotos: (isLoadedPhotos: Boolean) -> Unit
) {
    items(count = photos.itemCount,
        key = { index ->
            val photo = photos.peek(index)
            "${photo?.id ?: index}_$index"
        },
        contentType = photos.itemContentType()
    ) { index ->
        val photo = photos[index] ?: return@items
        val state = rememberPhotoItemUiState(
            index = rememberSaveable { mutableIntStateOf(index) },
            photo = rememberSaveable { mutableStateOf(photo) },
            visibleViewButton = rememberSaveable { mutableStateOf(true) },
            bookmarked = rememberSaveable {
                mutableStateOf(
                    bookmarkViewModel.isPhotoBookmarked(photo.id)
                )
            }
        )
        val padding = if (index == 0)
            2.dp
        else
            0.5.dp

        state.setIndex(index)
        state.setPhoto(photos[index]!!)
        state.setVisibleViewButton(true)
        state.setBookmark(bookmarkViewModel.isPhotoBookmarked(photos[index]!!.id))

        if (index == photos.itemCount - 1)
            onLoadedPhotos(true)

        PhotoItem(
            modifier = Modifier
                .padding(top = padding)
                .animateItem(tween(durationMillis = 250)),
            state = state,
            followViewModel = followViewModel,
            onShowUserInfo = onShowUserInfo,
            onItemClicked = onItemClicked,
            onViewPhotos = onViewPhotos
        )

        if (photos.itemCount < PAGE_SIZE_HINT && index == photos.itemCount - 1)
            Spacer(modifier = Modifier.height(26.dp))
    }
}

