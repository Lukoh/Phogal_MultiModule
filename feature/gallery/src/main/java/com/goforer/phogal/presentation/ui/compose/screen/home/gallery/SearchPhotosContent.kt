package com.goforer.phogal.presentation.ui.compose.screen.home.gallery

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.goforer.designsystem.animation.GenericCubicAnimationShape
import com.goforer.designsystem.component.Chips
import com.goforer.designsystem.theme.ColorSystemGray7
import com.goforer.designsystem.theme.PhogalTheme
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotoContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotosCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchSectionUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchPhotosSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.InitScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalPermissionsApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun SearchPhotosContent(
    modifier: Modifier = Modifier,
    contentUiState: SearchPhotoContentUiState,
    sectionUiState: SearchSectionUiState,
    paddingValues: PaddingValues,
    callbacks: SearchPhotosCallbacks
) {
    val recentWords by contentUiState.galleryViewModel.recentWords.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.clickable {
            contentUiState.baseUiState.keyboardController?.hide()
        }
    ) {
        SearchSection(
            modifier = Modifier.padding(2.dp, 0.dp, 2.dp, 0.dp),
            sectionUiState = sectionUiState,
            onSearched = { callbacks.onPerformSearch(it, false) }
        )

        // Sub-composables are stateless: they receive the values they need and
        // emit events back via callbacks. The holder is hidden from them.
        RecentWordsChips(
            recentWords = recentWords.asReversed(),
            isScrolling = contentUiState.scrolling,
            triggered = contentUiState.triggered,
            onTriggeredConsumed = { contentUiState.triggered = false },
            onChipClicked = { keyword ->
                sectionUiState.editableInputState.textState = keyword
                sectionUiState.wordChanged = true
                callbacks.onPerformSearch(keyword, true)
            }
        )
        PhotosOrInitScreen(
            paddingValues = paddingValues,
            contentUiState = contentUiState,
            callbacks = callbacks
        )
    }

    PermissionHandler(
        permissions = contentUiState.permissions,
        permissionVisible = contentUiState.permissionVisible,
        rationaleText = contentUiState.rationaleText,
        onPermissionGranted = {
            contentUiState.enabled = true
            contentUiState.permissionVisible = false
        },
        onPermissionDenied = {
            contentUiState.rationaleText = it
            contentUiState.enabled = false
            contentUiState.permissionVisible = true
        },
        onDialogDismissed = {
            contentUiState.enabled = false
            contentUiState.permissionVisible = false
        },
        onDialogConfirmed = { contentUiState.permissionVisible = false }
    )
}

/**
 * Animated row of recent search keywords. Hidden while scrolling. When a
 * `triggered` signal arrives, only the most recent keyword is shown (UX
 * requirement so the newly-committed keyword is highlighted without the full
 history noise).
 */
@Composable
private fun RecentWordsChips(
    recentWords: List<String>,
    isScrolling: Boolean,
    triggered: Boolean,
    onTriggeredConsumed: () -> Unit,
    onChipClicked: (String) -> Unit
) {
    LaunchedEffect(triggered) {
        if (triggered) {
            delay(1000.milliseconds)
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
    contentUiState: SearchPhotoContentUiState,
    callbacks: SearchPhotosCallbacks
) {
    val currentQuery by contentUiState.galleryViewModel.query.collectAsStateWithLifecycle()
    val searchingQuery by contentUiState.galleryViewModel.searchingQuery.collectAsStateWithLifecycle()
    val sessionId by contentUiState.galleryViewModel.searchSessionId.collectAsStateWithLifecycle()

    if (currentQuery.isNotBlank()) {
        key(searchingQuery, sessionId) {
            val photos = contentUiState.galleryViewModel.photos.collectAsLazyPagingItems()

            SearchPhotosSection(
                modifier = Modifier
                    .padding(top = 0.5.dp)
                    .weight(1f),
                paddingValues = paddingValues,
                sectionUiState = rememberSearchPhotosSectionUiState(photos),
                callbacks = callbacks
            )
        }
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
