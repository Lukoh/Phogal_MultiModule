package com.goforer.phogal.presentation.ui.compose.screen.home.setting.bookmark

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
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
import com.goforer.designsystem.component.CardSnackBar
import com.goforer.designsystem.component.CustomCenterAlignedTopAppBar
import com.goforer.designsystem.component.ScaffoldContent
import com.goforer.designsystem.component.dialog.ErrorDialog
import com.goforer.designsystem.theme.ColorBgSecondary
import com.goforer.designsystem.theme.PhogalTheme
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.presentation.stateholder.uistate.home.bookmark.BookmarkContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.bookmark.BookmarkScreenCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.bookmark.rememberBookmarkInternalCallbacks
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.UserInfoBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun BookmarkedPhotosScreen(
    modifier: Modifier = Modifier,
    contentUiState: BookmarkContentUiState,
    callbacks: BookmarkScreenCallbacks
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val internalCallbacks = rememberBookmarkInternalCallbacks(
        contentUiState = contentUiState,
        screenCallbacks = callbacks
    )

    BackHandler(enabled = true) {
        callbacks.onBackPressed()
    }

    DisposableEffect(contentUiState.baseUiState.lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                callbacks.onStart()
            } else if (event == Lifecycle.Event.ON_STOP) {
                callbacks.onStop()
            }
        }
        contentUiState.baseUiState.lifecycle.addObserver(observer)
        onDispose {
            contentUiState.baseUiState.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        contentColor = ColorBgSecondary,
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.navigationBarsPadding(),
                snackbar = { CardSnackBar(modifier = Modifier, it) }
            )
        },
        topBar = {
            CustomCenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.setting_bookmarked_photos),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 20.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            contentUiState.enabledLoadPhotos = false
                            callbacks.onBackPressed()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Profile"
                        )
                    }
                }
            )
        }, content = { paddingValues ->
            ScaffoldContent(topInterval = paddingValues.calculateTopPadding()) {
                BookmarkedPhotosContent(
                    modifier = modifier,
                    paddingValues = paddingValues,
                    photos = contentUiState.photos,
                    enabledLoadPhotos = contentUiState.enabledLoadPhotos,
                    callbacks = internalCallbacks.callbacks
                )
            }

            contentUiState.error?.let { error ->
                ErrorDialog(
                    title = stringResource(id = R.string.error_dialog_title),
                    text = error.message,
                    onDismiss = { contentUiState.error = null }
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
                                callbacks.onOpenWebView(user.firstName, it)
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
fun  BookmarkedPhotosScreenPreview() {
    PhogalTheme {
        Scaffold(
            contentColor = Color.White,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(id = R.string.setting_bookmarked_photos),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* doSomething() */ }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* doSomething() */ }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Bookmark"
                            )
                        }
                    }
                )
            }
        ) {
        }
    }
}
