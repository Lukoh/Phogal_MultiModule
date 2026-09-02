package com.goforer.phogal.presentation.ui.compose.screen.home.gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.goforer.designsystem.component.CardSnackBar
import com.goforer.designsystem.component.CustomCenterAlignedTopAppBar
import com.goforer.designsystem.component.ScaffoldContent
import com.goforer.base.utils.connect.ConnectionUtils
import com.goforer.phogal.core.ui.R
import androidx.compose.runtime.saveable.rememberSaveable
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotosContentUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.UserInfoBottomSheet
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.OfflineScreen
import com.goforer.designsystem.theme.ColorBgSecondary
import com.goforer.designsystem.theme.PhogalTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPhotosScreen(
    modifier: Modifier = Modifier,
    contentUiState: SearchPhotosContentUiState,
    onItemClicked: (id: String) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onStart: () -> Unit = {},
    onStop: () -> Unit = {},
) {
    if (!ConnectionUtils.isNetworkAvailable(contentUiState.baseUiState.context)) {
        OfflineScreen(modifier = Modifier)
    } else {
        val snackbarHostState = remember { SnackbarHostState() }
        // Stable lambdas. The capture set is the bare minimum needed for the
        // operation, which keeps Compose from invalidating these on every parent
        // recomposition.
        val snackbarHost = remember(snackbarHostState) {
            @Composable {
                SnackbarHost(
                    snackbarHostState,
                    snackbar = { snackbarData: SnackbarData ->
                        CardSnackBar(modifier = Modifier, snackbarData)
                    }
                )
            }
        }

        // Stable lambdas. The capture set is the bare minimum needed for the
        // operation, which keeps Compose from invalidating these on every parent
        // recomposition.
        val onSearch: (String) -> Unit = remember(contentUiState.galleryViewModel, contentUiState) {
            { keyword ->
                if (keyword.isNotEmpty() && (keyword != contentUiState.galleryUiState.currentQuery)) {
                    contentUiState.baseUiState.keyboardController?.hide()
                    contentUiState.galleryViewModel.onQueryChanged(keyword)
                    contentUiState.galleryViewModel.commitSearch()
                    contentUiState.setSearchTriggered()
                }
            }
        }

        // Note: SearchSection text input is now hoisted into rememberSearchSectionUiState
        // alongside the screen, so the chip-tap path goes through the same channel as
        // typed input. This collapses two state mutation paths into one.
        val sectionUiState = rememberSearchSectionUiState(enabled = remember { mutableStateOf(value = false) })

        // Stable lambdas. The capture set is the bare minimum needed for the
        // operation, which keeps Compose from invalidating these on every parent
        // recomposition.
        val onChipClicked: (String) -> Unit = remember(contentUiState.galleryViewModel, contentUiState,sectionUiState) {
            { keyword ->
                if (keyword.isNotEmpty() && (keyword != contentUiState.galleryUiState.currentQuery)) {
                    sectionUiState.editableInputState.textState = keyword
                    contentUiState.galleryViewModel.onQueryChanged(keyword, immediate = true)
                }
            }
        }

        ObserveLifecycle(
            lifecycleOwner = LocalLifecycleOwner.current,
            onStart = onStart,
            onStop = onStop
        )

        BackHandler(enabled = true) {
            (contentUiState.baseUiState.context as Activity).finish()
        }

        var selectedUserForInfo by rememberSaveable { mutableStateOf<User?>(null) }

        Scaffold(
            contentColor = ColorBgSecondary,
            snackbarHost = snackbarHost,
            topBar = {
                SearchTopBar(
                    showFavoriteAction = contentUiState.visibleActions,
                    onMenuClick = { /* TODO */ },
                    onFavoriteClick = { /* TODO */ }
                )
            },
            content = { paddingValues ->
                ScaffoldContent(topInterval = paddingValues.calculateTopPadding()) {
                    SearchPhotosContent(
                        modifier = modifier,
                        contentUiState = contentUiState,
                        paddingValues = paddingValues,
                        onSearch = onSearch,
                        onChipClicked = onChipClicked,
                        onShowUserInfo = { selectedUserForInfo = it },
                        onItemClicked = onItemClicked,
                        onViewPhotos = onViewPhotos,
                        onLoadResult = { isSuccessful, message ->
                            contentUiState.setActionsVisibilityChanged(isSuccessful)
                            if (!isSuccessful) {
                                contentUiState.baseUiState.scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        },
                    )
                }

                selectedUserForInfo?.let { user ->
                    val text = stringResource(id = R.string.user_info_has_no_portfolio)

                    UserInfoBottomSheet(
                        user = user,
                        showUserInfoBottomSheet = true,
                        onDismissedRequest = { isPortfolioClicked ->
                            selectedUserForInfo = null
                            if (isPortfolioClicked) {
                                val portfolioUrl = user.portfolioUrl
                                if (portfolioUrl.isNullOrEmpty()) {
                                    contentUiState.baseUiState.scope.launch {
                                        snackbarHostState.showSnackbar("${user.firstName} $text")
                                    }
                                } else {
                                    onOpenWebView(user.firstName, portfolioUrl)
                                }
                            }
                        }
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    showFavoriteAction: Boolean,
    onMenuClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    CustomCenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.bottom_navigation_gallery),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = FontFamily.SansSerif,
                fontSize = 20.sp,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Profile")
            }
        },
        actions = {
            if (showFavoriteAction) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Favorites"
                    )
                }
            }
        }
    )
}

/**
 * Side-effect wrapper that invokes [onStart] on ON_START and [onStop] on ON_STOP.
 * Uses `rememberUpdatedState` so the lambdas captured by the observer always
 * point at the latest version, not the one from the first composition.
 */
@Composable
private fun ObserveLifecycle(
    lifecycleOwner: LifecycleOwner,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStop by rememberUpdatedState(onStop)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> currentOnStart()
                Lifecycle.Event.ON_STOP -> currentOnStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    showSystemUi = true
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPhotosScreenPreview() {
    PhogalTheme {
        Scaffold(
            contentColor = Color.White,
            topBar = {
                // Notice: previewing a stateless TopBar requires only a Boolean
                // and two empty lambdas. No need to construct a holder, no
                // need to wrap a MutableState — pure input/output.
                SearchTopBar(
                    showFavoriteAction = true,
                    onMenuClick = {},
                    onFavoriteClick = {}
                )
            }
        ) { /* preview body */ }
    }
}
