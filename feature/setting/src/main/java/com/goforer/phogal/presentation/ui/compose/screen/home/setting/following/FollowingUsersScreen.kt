package com.goforer.phogal.presentation.ui.compose.screen.home.setting.following

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.goforer.designsystem.component.CardSnackBar
import com.goforer.designsystem.component.CustomCenterAlignedTopAppBar
import com.goforer.designsystem.component.ScaffoldContent
import com.goforer.designsystem.component.dialog.ErrorDialog
import com.goforer.designsystem.theme.ColorBgSecondary
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.presentation.stateholder.uistate.home.following.FollowingUserContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.following.FollowingUserScreenActions
import com.goforer.phogal.presentation.stateholder.uistate.home.following.rememberFollowingUserInternalActions

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FollowingUsersScreen(
    modifier: Modifier = Modifier,
    contentUiState: FollowingUserContentUiState,
    actions: FollowingUserScreenActions
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val internalActions = rememberFollowingUserInternalActions(
        contentUiState = contentUiState,
        screenActions = actions,
        snackbarHostState = snackbarHostState
    )

    BackHandler(enabled = true) {
        actions.onBackPressed()
    }

    DisposableEffect(contentUiState.baseUiState.lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                actions.onStart()
            } else if (event == Lifecycle.Event.ON_STOP) {
                actions.onStop()
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
                        text = stringResource(id = R.string.setting_follower),
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
                            contentUiState.setEnabledLoadPhotos(false)
                            actions.onBackPressed()
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
                FollowingUsersContent(
                    modifier = modifier,
                    paddingValues = paddingValues,
                    users = contentUiState.users,
                    enabledLoadPhotos = contentUiState.enabledLoadPhotos,
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
        }
    )
}
