package com.goforer.phogal.presentation.ui.compose.screen.home.gallery

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.goforer.designsystem.animation.GenericCubicAnimationShape
import com.goforer.designsystem.component.Chips
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotosContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchPhotosSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.InitScreen
import com.goforer.designsystem.theme.ColorSystemGray7
import com.goforer.designsystem.theme.PhogalTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalPermissionsApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun SearchPhotosContent(
    modifier: Modifier = Modifier,
    contentUiState: SearchPhotosContentUiState,
    paddingValues: PaddingValues,
    onSearch: (String) -> Unit,
    onChipClicked: (String) -> Unit,
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (id: String) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onLoadSuccess: (isSuccessful: Boolean) -> Unit
) {
    Column(
        modifier = modifier.clickable {
            contentUiState.baseUiState.keyboardController?.hide()
        }
    ) {
        SearchSection(
            modifier = Modifier.padding(2.dp, 0.dp, 2.dp, 0.dp),
            sectionUiState = rememberSearchSectionUiState(enabled = rememberSaveable { mutableStateOf(true) }),
            onSearched = onSearch
        )

        // Sub-composables are stateless: they receive the values they need and
        // emit events back via callbacks. The holder is hidden from them.
        RecentWordsChips(
            recentWords = contentUiState.galleryUiState.recentWords.asReversed(),
            isScrolling = contentUiState.scrolling,
            triggered = contentUiState.triggered,
            onTriggeredConsumed = contentUiState::setTriggerConsumed,
            onChipClicked = onChipClicked
        )
        PhotosOrInitScreen(
            paddingValues = paddingValues,
            query = contentUiState.galleryUiState.currentQuery,
            photos = contentUiState.galleryUiState.photos,
            onShowUserInfo = onShowUserInfo,
            onItemClicked = { photo, _ -> onItemClicked(photo.id) },
            onViewPhotos = onViewPhotos,
            onLoadSuccess = onLoadSuccess,
            onScroll = contentUiState::setScrollingChanged
        )
    }

    PermissionHandler(
        permissions = contentUiState.permissions,
        permissionVisible = contentUiState.permissionVisible,
        rationaleText = contentUiState.rationaleText,
        onPermissionGranted = contentUiState::setPermissionGranted,
        onPermissionDenied = contentUiState::setPermissionDenied,
        onDialogDismissed = contentUiState::setPermissionDialogDismissed,
        onDialogConfirmed = contentUiState::setPermissionDialogConfirmed
    )
}

/**
 * Animated row of recent search keywords. Hidden while scrolling. When a
 * `triggered` signal arrives, only the most recent keyword is shown (UX
 * requirement so the newly-committed keyword is highlighted without the full
 * history noise).
 */
@Composable
private fun RecentWordsChips(
    recentWords: List<String>,
    isScrolling: Boolean,
    triggered: Boolean,
    onTriggeredConsumed: () -> Unit,
    onChipClicked: (String) -> Unit
) {
    if (triggered) {
        LaunchedEffect(Unit) {
            onTriggeredConsumed()
        }
    }

    GenericCubicAnimationShape(
        visible = !isScrolling,
        duration = 100
    ) { animatedShape, visible ->
        if (!visible || recentWords.isEmpty()) return@GenericCubicAnimationShape

        val items = if (triggered) {
            listOf(recentWords.first())
        } else {
            recentWords
        }

        Chips(
            modifier = Modifier
                .padding(top = 4.dp)
                .graphicsLayer {
                    clip = true
                    shape = animatedShape
                },
            items = items,
            onClicked = onChipClicked
        )
    }
}

/**
 * Renders the paginated photo list when a query is active, or the
 * "tap to search" hint when the query is blank.
 */
@Composable
private fun ColumnScope.PhotosOrInitScreen(
    paddingValues: PaddingValues,
    query: String,
    photos: LazyPagingItems<Photo>,
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (Photo, Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onLoadSuccess: (Boolean) -> Unit,
    onScroll: (Boolean) -> Unit
) {
    if (query.isNotBlank()) {
        SearchPhotosSection(
            modifier = Modifier
                .padding(top = 0.5.dp)
                .weight(1f),
            paddingValues = paddingValues,
            photos = photos,
            sectionUiState = rememberSearchPhotosSectionUiState(rememberCoroutineScope(), rememberSaveable { mutableStateOf(false) }),
            onShowUserInfo = onShowUserInfo,
            onItemClicked = onItemClicked,
            onViewPhotos = onViewPhotos,
            onLoadSuccess = onLoadSuccess,
            onScroll = onScroll
        )
    } else {
        InitScreen(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.search_photos)
        )
    }
}

/**
 * Permission flow — **stateless**. Receives the visibility/text values and a
 * fan of typed callbacks for each transition. The previous version took the
 * full `SearchPhotosContentUiState` and wrote `.value = ...` against four of
 * its `MutableState` fields; that coupling is gone.
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PermissionHandler(
    permissions: List<String>,
    permissionVisible: Boolean,
    rationaleText: String,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: (rationale: String) -> Unit,
    onDialogDismissed: () -> Unit,
    onDialogConfirmed: () -> Unit
) {
    val multiplePermissionsState: MultiplePermissionsState =
        rememberMultiplePermissionsState(permissions)

    CheckPermission(
        multiplePermissionsState = multiplePermissionsState,
        onPermissionGranted = onPermissionGranted,
        onPermissionNotGranted = onPermissionDenied
    )

    if (permissionVisible) {
        PermissionBottomSheet(
            rationaleText = rationaleText,
            onDismissedRequest = onDialogDismissed,
            onClicked = {
                multiplePermissionsState.launchMultiplePermissionRequest()
                onDialogConfirmed()
            }
        )
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    showSystemUi = true
)
@Composable
fun PhotosContentPreview(modifier: Modifier = Modifier) {
    PhogalTheme {
        BoxWithConstraints(modifier = modifier) {
            val isWideScreen = maxWidth > 600.dp
            val dynamicHorizontalPadding = if (isWideScreen) 16.dp else 8.dp
            val dynamicTextStyle = if (isWideScreen) {
                typography.headlineSmall
            } else {
                typography.titleMedium
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchSection(
                    modifier = Modifier.padding(horizontal = dynamicHorizontalPadding),
                    onSearched = { }
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.search_photos),
                        style = dynamicTextStyle.copy(
                            color = ColorSystemGray7,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
