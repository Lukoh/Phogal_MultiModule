package com.goforer.phogal.presentation.ui.compose.screen.home.gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.goforer.base.utils.connect.ConnectionUtils
import com.goforer.designsystem.component.CardSnackBar
import com.goforer.designsystem.component.CustomCenterAlignedTopAppBar
import com.goforer.designsystem.component.ScaffoldContent
import com.goforer.designsystem.component.dialog.ErrorDialog
import com.goforer.designsystem.theme.ColorBgSecondary
import com.goforer.designsystem.theme.PhogalTheme
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotosContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.SearchPhotosScreenActions
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchPhotosInternalActions
import com.goforer.phogal.presentation.stateholder.uistate.home.gallery.rememberSearchSectionUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.OfflineScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.UserInfoBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPhotosScreen(
    modifier: Modifier = Modifier,
    contentUiState: SearchPhotosContentUiState,
    actions: SearchPhotosScreenActions
) {
    if (!ConnectionUtils.isNetworkAvailable(contentUiState.baseUiState.context)) {
        OfflineScreen(modifier = Modifier)
    } else {
        val snackbarHostState = remember { SnackbarHostState() }
        val sectionUiState = rememberSearchSectionUiState(
            enabled = rememberSaveable { mutableStateOf(contentUiState.enabled) }
        )

        LaunchedEffect(contentUiState.enabled) {
            sectionUiState.setEnabled(contentUiState.enabled)
        }

        // Encapsulate all logic and stable callbacks in a single object.
        // This dramatically reduces variable declarations in the screen body.
        val internalActions = rememberSearchPhotosInternalActions(
            contentUiState = contentUiState,
            sectionUiState = sectionUiState,
            screenActions = actions,
            snackbarHostState = snackbarHostState
        )

        ObserveLifecycle(
            lifecycleOwner = LocalLifecycleOwner.current,
            onStart = actions.onStart,
            onStop = actions.onStop
        )

        BackHandler(enabled = true) {
            (contentUiState.baseUiState.context as Activity).finish()
        }

        val layoutDirection = LocalLayoutDirection.current

        Scaffold(
            contentColor = ColorBgSecondary,
            snackbarHost = {
                SnackbarHost(
                    snackbarHostState,
                    snackbar = { CardSnackBar(modifier = Modifier, it) }
                )
            },
            topBar = {
                SearchTopBar(
                    showFavoriteActionProvider = { contentUiState.visibleActions },
                    onMenuClick = internalActions.onMenuClick,
                    onFavoriteClick = internalActions.onFavoriteClick
                )
            },
            content = { paddingValues ->
                ScaffoldContent(topInterval = paddingValues.calculateTopPadding()) {
                    SearchPhotosContent(
                        modifier = modifier,
                        contentUiState = contentUiState,
                        sectionUiState = sectionUiState,
                        paddingValues = PaddingValues(
                            start = paddingValues.calculateStartPadding(layoutDirection),
                            top = 0.dp,
                            end = paddingValues.calculateEndPadding(layoutDirection),
                            bottom = paddingValues.calculateBottomPadding()
                        ),
                        actions = internalActions.actions
                    )
                }

                contentUiState.error?.let { error ->
                    ErrorDialog(
                        title = stringResource(id = R.string.error_dialog_title),
                        text = error.message,
                        onDismiss = { contentUiState.setError(null) }
                    )
                }

                contentUiState.selectedUser?.let { user ->
                    UserInfoBottomSheet(
                        user = user,
                        showUserInfoBottomSheet = true,
                        onDismissedRequest = { isPortfolioClicked ->
                            contentUiState.selectedUser = null
                            if (isPortfolioClicked) {
                                user.portfolioUrl?.let {
                                    actions.onOpenWebView(user.firstName, it)
                                } ?: run {
                                    contentUiState.baseUiState.scope.launch {
                                        val text = contentUiState.baseUiState.context.getString(R.string.user_info_has_no_portfolio)
                                        snackbarHostState.showSnackbar("${user.firstName} $text")
                                    }
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
    showFavoriteActionProvider: () -> Boolean,
    onMenuClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    val showFavoriteAction = showFavoriteActionProvider()

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

@Composable
private fun ObserveLifecycle(
    lifecycleOwner: LifecycleOwner,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_START) {
            onStart()
        } else if (event == Lifecycle.Event.ON_STOP) {
            onStop()
        }
    }
    DisposableEffect(lifecycleOwner) {
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
                SearchTopBar(
                    showFavoriteActionProvider = { true },
                    onMenuClick = {},
                    onFavoriteClick = {}
                )
            }
        ) { /* preview body */ }
    }
}
