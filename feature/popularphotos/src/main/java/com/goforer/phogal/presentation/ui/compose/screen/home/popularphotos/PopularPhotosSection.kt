@file:Suppress("UNCHECKED_CAST")

package com.goforer.phogal.presentation.ui.compose.screen.home.popularphotos

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.designsystem.component.paging.PagingLoadStateCallbacks
import com.goforer.designsystem.component.paging.PagingLoadStateEffect
import com.goforer.designsystem.component.paging.PagingRenderCallbacks
import com.goforer.designsystem.component.paging.PagingRenderState
import com.goforer.designsystem.component.paging.contentItems
import com.goforer.designsystem.component.paging.rememberIsScrolledPastThreshold
import com.goforer.designsystem.component.paging.rememberLazyListState
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
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.PopularPhotosCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.PopularPhotosSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.rememberPopularPhotosSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.LoadingPicture
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.PhotoItem
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.ShowUpButton
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun PopularPhotosSection(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    sectionUiState: PopularPhotosSectionUiState,
    callbacks: PopularPhotosCallbacks
) {
    PopularPhotosSectionContent(
        modifier = modifier,
        paddingValues = paddingValues,
        sectionUiState = sectionUiState,
        callbacks = callbacks
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PopularPhotosSectionContent(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    sectionUiState: PopularPhotosSectionUiState,
    callbacks: PopularPhotosCallbacks
) {
    val isRefreshing by remember(sectionUiState.photos.loadState.refresh, sectionUiState.manualRefreshing) {
        derivedStateOf {
            sectionUiState.manualRefreshing && sectionUiState.photos.loadState.refresh is LoadState.Loading
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
                onPaginationReached = { callbacks.onLoadedPhotos(true) }
            )
        },
        logTag = "PopularPhotosSection"
    )

    // derivedStateOf: only triggers recomposition when the boolean actually flips,
    // not on every scroll tick.
    val isScrolledPastThreshold = sectionUiState.lazyListState.rememberIsScrolledPastThreshold()
    val layoutDirection = LocalLayoutDirection.current

    // Material 3 PullToRefreshBox — default indicator is rendered automatically.
    PullToRefreshBox(
        modifier = modifier.clip(RoundedCornerShape(2.dp)),
        isRefreshing = isRefreshing,
        onRefresh = {
            sectionUiState.manualRefreshing = true
            sectionUiState.photos.refresh()
        }
    ) {
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
                    bottom = (paddingValues.calculateBottomPadding() + 58.dp).coerceAtLeast(0.dp)
                )
            ) {
                renderLoadState(
                    modifier = modifier,
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
                        bottom = (paddingValues.calculateBottomPadding() - 18.dp).coerceAtLeast(0.dp)
                    ),
                visible = isScrolledPastThreshold,
                onClick = {
                    sectionUiState.scope.launch {
                        sectionUiState.lazyListState.animateScrollToItem(0)
                    }
                }
            )
        }
    }
}

/**
 * Dispatches the current [LoadState] of [PopularPhotosSectionUiState.photos] into the appropriate sub-renderer.
 * Kept as a LazyListScope extension so each sub-renderer can emit `item {}` / `items {}`
 * directly without re-wrapping.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.renderLoadState(
    modifier: Modifier = Modifier,
    sectionUiState: PopularPhotosSectionUiState,
    callbacks: PopularPhotosCallbacks,
    isInspectionMode: Boolean,
    isResettingScroll: Boolean
) {
    val isInitiallyLoading = sectionUiState.photos.loadState.refresh is LoadState.Loading
    val isPopulatingInitial = sectionUiState.photos.itemCount < 20 // PAGE_SIZE
    val suppressAnimation = isInitiallyLoading || isInspectionMode || isResettingScroll || isPopulatingInitial

    renderPagingLoadState(
        state = PagingRenderState(
            items = sectionUiState.photos,
            loadingDone = sectionUiState.loadingDone,
            isInitialRefresh = !sectionUiState.manualRefreshing
        ),
        modifier = modifier,
        content = {
            contentItems(
                items = sectionUiState.photos,
                key = { _, photo -> "${sectionUiState.sessionId}_${photo.id}" },
                content = { padding, index, photo ->
                    PhotoItem(
                        modifier = Modifier
                            .padding(top = padding)
                            .then(
                                if (suppressAnimation) Modifier else Modifier.animateItem(tween(durationMillis = 250))
                            ),
                        state = rememberPhotoItemUiState(
                            photo = photo,
                            index = index,
                            initialVisibleViewButton = true,
                            initialBookmarked = callbacks.isPhotoBookmarked(photo.id)
                        ),
                        isFollowed = callbacks.isUserFollowed(photo.user.id),
                        callbacks = remember(callbacks) {
                            PhotoItemCallbacks(
                                onFollowClick = callbacks.onToggleFollow,
                                onShowUserInfo = callbacks.onShowUserInfo,
                                onItemClicked = { p, i -> callbacks.onItemClicked(p.id, i) },
                                onViewPhotos = callbacks.onViewPhotos
                            )
                        }
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

@Preview(name = "Light Mode", showBackground = true)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun PopularPhotosSectionPreview() {
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
    val pagingData = PagingData.from(listOf(mockPhoto, mockPhoto.copy(id = "2")))
    val photos = flowOf(pagingData).collectAsLazyPagingItems()

    PhogalTheme {
        PopularPhotosSectionContent(
            paddingValues = PaddingValues(all = 0.dp),
            sectionUiState = rememberPopularPhotosSectionUiState(photos, sessionId = 0),
            callbacks = PopularPhotosCallbacks(
                isUserFollowed = { false },
                isPhotoBookmarked = { false },
                onToggleFollow = {},
                onShowUserInfo = {},
                onItemClicked = { _, _ -> },
                onViewPhotos = { _, _, _, _ -> },
                onLoadResult = { },
                onLoadedPhotos = {}
            )
        )
    }
}
