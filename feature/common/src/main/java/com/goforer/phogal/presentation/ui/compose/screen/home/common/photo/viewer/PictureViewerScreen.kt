package com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.viewer

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.goforer.designsystem.theme.Red60
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.presentation.stateholder.business.home.common.photo.info.PictureViewModel
import com.goforer.phogal.presentation.stateholder.uistate.ErrorEntity
import com.goforer.phogal.presentation.stateholder.uistate.UiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.PhotoContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.PictureViewerScreenActions
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPictureViewerInternalActions
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.UserInfoBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PictureViewerScreen(
    modifier: Modifier = Modifier,
    contentUiState: PhotoContentUiState,
    actions: PictureViewerScreenActions
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val internalActions = rememberPictureViewerInternalActions(
        contentUiState = contentUiState,
        screenActions = actions,
        snackbarHostState = snackbarHostState
    )

    BackHandler(enabled = true) { internalActions.onBackPressed() }
    DisposableEffect(contentUiState.baseUiState.lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> actions.onStart()
                Lifecycle.Event.ON_STOP  -> actions.onStop()
                else -> Unit
            }
        }
        contentUiState.baseUiState.lifecycle.addObserver(observer)
        onDispose { contentUiState.baseUiState.lifecycle.removeObserver(observer) }
    }

    // Kick off the load whenever the id changes
    LaunchedEffect(contentUiState.id) {
        contentUiState.pictureViewModel.loadPicture(contentUiState.id)
    }

    // Top-bar icons read from the authoritative pictureUiState.
    val currentPicture = (contentUiState.pictureState as? UiState.Success)?.data
    val isLikedByUser = currentPicture?.likedByUser == true

    // Observe like/unlike transient result so we can surface an error dialog.
    LikeActionHandle(pictureViewModel = contentUiState.pictureViewModel)

    Scaffold(
        contentColor = Color.White,
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
                    IconButton(onClick = internalActions.onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Picture"
                        )
                    }
                },
                actions = {
                    if (contentUiState.visibleActions && (currentPicture != null)) {
                        IconButton(
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (isLikedByUser) Red60 else Color.Black
                            ),
                            onClick = internalActions.onLikedClick
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

                        IconButton(
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (contentUiState.enabledBookmark) {
                                    Red60
                                } else {
                                    Color.Black
                                }
                            ),
                            onClick = {
                                internalActions.onBookmarkClick(currentPicture)
                            }
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
            ScaffoldContent(0.dp) {
                val isFollowed = currentPicture?.let { actions.isUserFollowed(it.user) } ?: false

                PictureViewerContent(
                    modifier = modifier,
                    contentPadding = paddingValues,
                    pictureState = contentUiState.pictureState,
                    trackDownloadState = contentUiState.trackDownloadState,
                    showPopup = contentUiState.showPopup,
                    dialogState = contentUiState.dialogState,
                    visibleViewButton = contentUiState.visibleViewButton,
                    isFollowed = isFollowed,
                    actions = internalActions.viewerActions
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

/**
 * Observes the transient [PictureViewModel.likeActionState]
 * and shows an error dialog on failure.
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
                fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart),
    ) {
        val error = errorState.error

        ErrorDialog(
            title = if (error is ErrorEntity.Network) {
                stringResource(id = R.string.error_dialog_network_title)
            } else {
                stringResource(id = R.string.error_dialog_title)
            },
            text = error.message
        ) {
            showErrorDialog.value = false
            pictureViewModel.consumeLikeAction()
        }
    }
}
