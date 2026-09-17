package com.goforer.phogal.presentation.ui.compose.screen.home.common.user.userphotos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.goforer.designsystem.component.CardSnackBar
import com.goforer.designsystem.component.CustomCenterAlignedTopAppBar
import com.goforer.designsystem.component.ScaffoldContent
import com.goforer.designsystem.component.dialog.ErrorDialog
import com.goforer.designsystem.theme.ColorBgSecondary
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.UserPhotoContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.UserPhotosScreenCallbacks
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.rememberUserPhotosInternalCallbacks
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.UserInfoBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPhotosScreen(
    modifier: Modifier = Modifier,
    contentUiState: UserPhotoContentUiState,
    callbacks: UserPhotosScreenCallbacks
) {
    if (contentUiState.name.isNotBlank()) {
        LaunchedEffect(contentUiState.name) {
            contentUiState.userPhotosViewModel.loadFor(contentUiState.name)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val internalCallbacks = rememberUserPhotosInternalCallbacks(
        contentUiState = contentUiState,
        screenCallbacks = callbacks,
        snackbarHostState = snackbarHostState
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
            CustomCenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "${contentUiState.firstName}${" "}${stringResource(id = R.string.picture_photos)}",
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
                            contentUiState.visible = false
                            callbacks.onBackPressed()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (contentUiState.visible) {
                        IconButton(onClick = { /* doSomething() */ }) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Favorite"
                            )
                        }
                    }
                }
            )
        },
        content = { paddingValues ->
            ScaffoldContent(topInterval = paddingValues.calculateTopPadding()) {
                UserPhotosContent(
                    modifier = modifier,
                    paddingValues = PaddingValues(
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        top = 0.dp,
                        end = paddingValues.calculateEndPadding(layoutDirection),
                        bottom = paddingValues.calculateBottomPadding()
                    ),
                    contentUiState = contentUiState,
                    photos = contentUiState.photos,
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
