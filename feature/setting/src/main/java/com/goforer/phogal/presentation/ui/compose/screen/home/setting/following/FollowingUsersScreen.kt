package com.goforer.phogal.presentation.ui.compose.screen.home.setting.following

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import com.goforer.base.extension.isNull
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.presentation.stateholder.uistate.home.setting.following.FollowingUserContentUiState
import com.goforer.designsystem.theme.ColorBgSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FollowingUsersScreen(
    modifier: Modifier = Modifier,
    contentUiState: FollowingUserContentUiState,
    onBackPressed: () -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onStart: () -> Unit = {
        //To Do:: Implement the code what you want to do....
    },
    onStop: () -> Unit = {
        //To Do:: Implement the code what you want to do....
    }
) {
    val currentOnStart by rememberUpdatedState(onStart)
    val currentOnStop by rememberUpdatedState(onStop)
    val snackbarHostState = remember { SnackbarHostState() }
    val backHandlingEnabled by remember { mutableStateOf(true) }
    val text = stringResource(id = R.string.user_info_has_no_portfolio)
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
    val onOpenWebView = remember {
        { firstName: String, url: String? ->
            url.isNull({
                contentUiState.baseUiState.scope.launch {
                    snackbarHostState.showSnackbar("${firstName}${" "}${text}")
                }
            }, {
                onOpenWebView(firstName, it)
            })
        }
    }

    BackHandler(backHandlingEnabled) {
        onBackPressed()
    }

    DisposableEffect(contentUiState.baseUiState.lifecycle) {
        // Create an observer that triggers our remembered callbacks
        // for doing anything
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                currentOnStart()
            } else if (event == Lifecycle.Event.ON_STOP) {
                currentOnStop()
            }
        }

        // Add the observer to the lifecycle
        contentUiState.baseUiState.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            contentUiState.baseUiState.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        contentColor = ColorBgSecondary,
        snackbarHost = snackbarHost,
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
                            onBackPressed()
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
                    onViewPhotos = onViewPhotos,
                    onOpenWebView = onOpenWebView,
                    onFollow = {
                        contentUiState.followViewModel.isUserFollowed(it)
                    },
                )
            }
        }
    )
}