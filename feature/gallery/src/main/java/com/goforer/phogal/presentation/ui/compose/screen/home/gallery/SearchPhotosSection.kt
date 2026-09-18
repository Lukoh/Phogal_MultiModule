package com.goforer.phogal.presentation.ui.compose.screen.home.gallery

import android.content.res.Configuration
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.designsystem.component.paging.PagingLoadStateCallbacks
import com.goforer.designsystem.component.paging.PagingLoadStateEffect
import com.goforer.designsystem.component.paging.ScrollSignalEffect
import com.goforer.designsystem.component.paging.contentItems
import com.goforer.designsystem.component.paging.rememberIsScrolledPastThreshold
import com.goforer.designsystem.component.paging.renderPagingLoadState
import com.goforer.designsystem.theme.Blue15
import com.goforer.designsystem.theme.Blue95
import com.goforer.designsystem.theme.PhogalTheme
import com.goforer.phogal.data.model.remote.response.gallery.common.ProfileImage
import com.goforer.phogal.data.model.remote.response.gallery.common.Urls
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.PhotoItemCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPhotoItemUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotosCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotosSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchPhotosSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.LoadingPicture
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.PhotoItem
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber

@Composable
fun SearchPhotosSection(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    sectionUiState: SearchPhotosSectionUiState,
    callbacks: SearchPhotosCallbacks
) {
    SearchPhotosSectionContent(
        modifier = modifier,
        paddingValues = paddingValues,
        sectionUiState = sectionUiState,
        callbacks = callbacks
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchPhotosSectionContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    sectionUiState: SearchPhotosSectionUiState,
    callbacks: SearchPhotosCallbacks
) {
    val isRefreshing by remember(sectionUiState.photos.loadState.refresh, sectionUiState.manualRefreshing) {
        derivedStateOf {
            sectionUiState.manualRefreshing && sectionUiState.photos.loadState.refresh is LoadState.Loading
        }
    }

    // Note: The parent key(query, sessionId) block ensures that this component
    // starts with a fresh LazyListState at index 0 whenever a new search begins.
    // The logic below handles scroll resets for standard paging events like pagination
    // or manual refresh within the same query session.
    LaunchedEffect(
        sectionUiState.photos.loadState.refresh,
        sectionUiState.photos.itemCount
    ) {
        val refreshState = sectionUiState.photos.loadState.refresh
        val itemCount = sectionUiState.photos.itemCount

        when (refreshState) {
            is LoadState.Loading -> {
                if (itemCount > 0) {
                    sectionUiState.isResettingScroll = true
                }
            }

            is LoadState.NotLoading -> {
                sectionUiState.resetScrollIfNeeded(itemCount)
            }

            else -> Unit
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
        logTag = "SearchPhotosSection"
    )

    // derivedStateOf: only triggers recomposition when the boolean actually flips,
    // not on every scroll tick.
    val isScrolledPastThreshold = sectionUiState.lazyListState.rememberIsScrolledPastThreshold()

    // Propagate scroll signal to parent — only when isScrollInProgress changes,
    // not on every pixel of scrolling.
    sectionUiState.lazyListState.ScrollSignalEffect(callbacks.onScroll)

    // Nav3-stable Material 3 PullToRefreshBox replaces the deprecated
    // androidx.compose.material.pullrefresh.* APIs. The container handles the
    // refresh indicator itself — no separate PullRefreshIndicator needed.
    PullToRefreshBox(
        modifier = modifier.clip(RoundedCornerShape(2.dp)),
        isRefreshing = isRefreshing,
        onRefresh = {
            sectionUiState.manualRefreshing = true
            sectionUiState.photos.refresh()
        }
    ) {
        val layoutDirection = LocalLayoutDirection.current
        val isDark = isSystemInDarkTheme()
        val skyBlueBackground = if (isDark)
            Blue15
        else
            Blue95

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val isInspectionMode = LocalInspectionMode.current

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(skyBlueBackground),
                state = sectionUiState.lazyListState,
                contentPadding = PaddingValues(
                    start = paddingValues.calculateStartPadding(layoutDirection).coerceAtLeast(0.dp),
                    top = paddingValues.calculateTopPadding().coerceAtLeast(0.dp),
                    end = paddingValues.calculateEndPadding(layoutDirection).coerceAtLeast(0.dp),
                    bottom = (paddingValues.calculateBottomPadding() + 46.dp).coerceAtLeast(0.dp)
                )
            ) {
                renderLoadState(
                    sectionUiState = sectionUiState,
                    callbacks = callbacks,
                    isInspectionMode = isInspectionMode,
                    isResettingScroll = sectionUiState.isResettingScroll
                )
            }

            // Show up-button only when user has scrolled past the threshold and isn't
            // actively scrolling (prevents the button from flickering during drags).
            ShowUpButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 4.dp,
                        bottom = (paddingValues.calculateBottomPadding() - 18.dp).coerceAtLeast(0.dp)
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
 * Dispatches the current [LoadState] of [SearchPhotosSectionUiState.photos] into the appropriate sub-renderer.
 * Kept as a LazyListScope extension so each sub-renderer can emit `item {}` / `items {}`
 * directly without re-wrapping.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.renderLoadState(
    sectionUiState: SearchPhotosSectionUiState,
    callbacks: SearchPhotosCallbacks,
    isInspectionMode: Boolean,
    isResettingScroll: Boolean
) {
    val isInitiallyLoading = sectionUiState.photos.loadState.refresh is LoadState.Loading
    val suppressAnimation = isInitiallyLoading || isInspectionMode || isResettingScroll

    renderPagingLoadState(
        items = sectionUiState.photos,
        loadingDone = sectionUiState.loadingDone,
        content = {
            contentItems(
                items = sectionUiState.photos,
                key = { _, photo -> photo.id },
                content = { padding, index, photo ->
                    PhotoItem(
                        modifier = Modifier
                            .padding(top = padding)
                            .then(
                                if (suppressAnimation) Modifier else Modifier.animateItem(tween(durationMillis = 200))
                            ),
                        state = rememberPhotoItemUiState(
                            photo = photo,
                            index = index,
                            initialVisibleViewButton = true,
                            initialBookmarked = callbacks.isPhotoBookmarked(photo.id)
                        ),
                        isFollowed = callbacks.isUserFollowed(photo.user),
                        callbacks = remember(callbacks) {
                            PhotoItemCallbacks(
                                onFollowClick = callbacks.onToggleFollow,
                                onShowUserInfo = callbacks.onShowUserInfo,
                                onItemClicked = { p, _ -> callbacks.onItemClicked(p.id) },
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

@Preview(name = "Light Mode", showBackground = true)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun SearchPhotosSectionPreview() {
    val mockUser = User.empty().copy(
        name = "John Doe",
        username = "johndoe",
        profileImage = ProfileImage.empty().copy(medium = "")
    )
    val mockPhoto = Photo.empty().copy(
        id = "1",
        user = mockUser,
        urls = Urls.empty().copy(regular = ""),
        width = 1080,
        height = 720
    )
    val pagingData = PagingData.from(listOf(mockPhoto, mockPhoto))
    val photos = flowOf(pagingData).collectAsLazyPagingItems()

    PhogalTheme {
        SearchPhotosSectionContent(
            modifier = Modifier.fillMaxSize(),
            paddingValues = PaddingValues(all = 0.dp),
            sectionUiState = rememberSearchPhotosSectionUiState(photos),
            callbacks = SearchPhotosCallbacks(
                onPerformSearch = { _, _ -> },
                isUserFollowed = { false },
                isPhotoBookmarked = { false },
                onToggleFollow = {},
                onShowUserInfo = {},
                onItemClicked = {},
                onViewPhotos = { _, _, _, _ -> },
                onLoadResult = {},
                onScroll = {},
                onMenuClick = {},
                onFavoriteClick = {}
            )
        )
    }
}
