package com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.viewer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goforer.designsystem.component.CardSnackBar
import com.goforer.designsystem.component.CustomCenterAlignedTopAppBar
import com.goforer.designsystem.component.ScaffoldContent
import com.goforer.designsystem.component.dialog.ErrorDialog
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.business.home.common.photo.info.PictureViewModel
import com.goforer.phogal.presentation.stateholder.uistate.UiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.PhotoContentUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.UserInfoBottomSheet
import com.goforer.designsystem.theme.Red60
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PictureViewerScreen(
    modifier: Modifier = Modifier,
    contentUiState: PhotoContentUiState,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onBackPressed: () -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onStart: () -> Unit = {},
    onStop: () -> Unit = {}
) {
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStop by rememberUpdatedState(onStop)
    val snackbarHostState = remember { SnackbarHostState() }
    val backHandlingEnabled by remember { mutableStateOf(true) }

    BackHandler(backHandlingEnabled) { onBackPressed() }

    DisposableEffect(contentUiState.baseUiState.lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> currentOnStart()
                Lifecycle.Event.ON_STOP  -> currentOnStop()
                else -> Unit
            }
        }
        contentUiState.baseUiState.lifecycle.addObserver(observer)
        onDispose { contentUiState.baseUiState.lifecycle.removeObserver(observer) }
    }

    // Kick off the load whenever the id changes
    // legacy `enabledLoadState` one-shot gate).
    LaunchedEffect(contentUiState.id) {
        contentUiState.pictureViewModel.loadPicture(contentUiState.id)
    }

    // Top-bar icons read from the authoritative pictureUiState.
    val currentPicture = (contentUiState.pictureState as? UiState.Success)?.data
    val isLikedByUser = currentPicture?.likedByUser == true
    // Stable lambdas. The capture set is the bare minimum needed for the
    // operation, which keeps Compose from invalidating these on every parent
    // recomposition.


    // Stable lambdas. The capture set is the bare minimum needed for the
    // operation, which keeps Compose from invalidating these on every parent
    // recomposition.
    val onShownPhoto: (Picture) -> Unit = remember(contentUiState.bookmarkViewModel, contentUiState) {
        { picture: Picture ->
            contentUiState.setVisibleActions(true)
            contentUiState.baseUiState.scope.launch {
                contentUiState.setEnabledBookmark(contentUiState.bookmarkViewModel.isPhotoBookmarked(picture))
            }
        }
    }

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

    // Observe like/unlike transient result so we can surface an error dialog.
    LikeActionHandle(pictureViewModel = contentUiState.pictureViewModel)

    Scaffold(
        contentColor = Color.White,
        snackbarHost = snackbarHost,
        topBar = {
            CustomCenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.picture_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 20.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Picture"
                        )
                    }
                },
                actions = {
                    if (contentUiState.visibleActions && currentPicture != null) {
                        // Stable lambdas. The capture set is the bare minimum needed for the
                        // operation, which keeps Compose from invalidating these on every parent
                        // recomposition.
                        val onLikedClick = remember(contentUiState.pictureViewModel) {
                            {
                                contentUiState.pictureViewModel.toggleLike()
                            }
                        }

                        IconButton(
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (isLikedByUser) Red60 else Color.Black
                            ),
                            onClick = onLikedClick
                        ) {
                            Icon(
                                imageVector = if (isLikedByUser) {
                                    ImageVector.vectorResource(id = R.drawable.ic_like_on)
                                } else {
                                    ImageVector.vectorResource(id = R.drawable.ic_like_off)
                                },
                                contentDescription = "Like"
                            )
                        }

                        // Stable lambdas. The capture set is the bare minimum needed for the
                        // operation, which keeps Compose from invalidating these on every parent
                        // recomposition.
                        val onEnabledClick = remember(contentUiState.bookmarkViewModel, contentUiState) {
                            {
                                contentUiState.bookmarkViewModel.setBookmarkPicture(currentPicture)
                                contentUiState.setEnabledBookmark(!contentUiState.enabledBookmark)
                            }
                        }

                        IconButton(
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (contentUiState.enabledBookmark) {
                                    Red60
                                } else {
                                    Color.Black
                                }
                            ),
                            onClick = onEnabledClick
                        ) {
                            Icon(
                                imageVector = if (contentUiState.enabledBookmark) {
                                    ImageVector.vectorResource(id = R.drawable.ic_bookmark_on)
                                } else {
                                    ImageVector.vectorResource(id = R.drawable.ic_bookmark_off)
                                },
                                contentDescription = "Bookmark"
                            )
                        }
                    }
                }
            )
        },
        content = { paddingValues ->
            var selectedUserForInfo by rememberSaveable { mutableStateOf<User?>(null) }

            ScaffoldContent(0.dp) {
                PictureViewerContent(
                    modifier = modifier,
                    contentPadding = paddingValues,
                    contentUiState = contentUiState,
                    onShowUserInfo = { selectedUserForInfo = it },
                    onViewPhotos = onViewPhotos,
                    onShownPhoto = onShownPhoto,
                    onSuccess = { isSuccessful: Boolean ->
                        if (!isSuccessful) contentUiState.setVisibleActions(false)
                    }
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
                                    snackbarHostState.showSnackbar("${user.firstName} ${text}")
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

/**
 * Observes the transient [PictureViewModel.likeActionState]
 * and shows an error dialog on failure. Replaces the legacy `LikeResponseHandle` +
 * `UnlikeResponseHandle` pair — one handler is enough because the VM now exposes a
 * single state for both the POST and the DELETE path.
 */
@Composable
private fun LikeActionHandle(pictureViewModel: PictureViewModel) {
    val likeActionState by pictureViewModel.likeActionState.collectAsStateWithLifecycle()
    val showErrorDialog = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(likeActionState) {
        if (likeActionState is UiState.Error) showErrorDialog.value = true
    }

    val errorState = likeActionState as? UiState.Error ?: return
    if (!showErrorDialog.value) return

    AnimatedVisibility(
        visible = true,
        modifier = Modifier,
        enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) +
            fadeIn() + expandIn(expandFrom = Alignment.TopStart),
        exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) +
            fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
    ) {
        ErrorDialog(
            title = if (errorState.code !in 200..299) {
                stringResource(id = R.string.error_dialog_network_title)
            } else {
                stringResource(id = R.string.error_dialog_title)
            },
            text = errorState.message
        ) {
            showErrorDialog.value = false
            pictureViewModel.consumeLikeAction()
        }
    }
}
