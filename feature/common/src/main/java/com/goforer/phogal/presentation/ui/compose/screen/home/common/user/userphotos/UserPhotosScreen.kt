package com.goforer.phogal.presentation.ui.compose.screen.home.common.user.userphotos

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.goforer.base.customtab.openCustomTab
import com.goforer.designsystem.component.CardSnackBar
import com.goforer.designsystem.component.CustomCenterAlignedTopAppBar
import com.goforer.designsystem.component.ScaffoldContent
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.UserInfoBottomSheet
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.photos.UserPhotosContentUiState
import com.goforer.designsystem.theme.ColorBgSecondary
import com.goforer.designsystem.theme.PhogalTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPhotosScreen(
    modifier: Modifier = Modifier,
    contentUiState: UserPhotosContentUiState,
    onItemClicked: (id: String) -> Unit,
    onBackPressed: () -> Unit,
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
    // Stable lambdas. The capture set is the bare minimum needed for the
    // operation, which keeps Compose from invalidating these on every parent
    // recomposition.
    val snackbarHost = remember(snackbarHostState) {
        @Composable {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.navigationBarsPadding(),
                snackbar = { snackbarData: SnackbarData ->
                    CardSnackBar(modifier = Modifier, snackbarData)
                }
            )
        }
    }



    // Kick off the Paging stream whenever the target user changes.
    LaunchedEffect(contentUiState.name) {
        if (contentUiState.name.isNotBlank()) {
            contentUiState.userPhotosViewModel.loadFor(contentUiState.name)
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
                        onClick = { onBackPressed() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (contentUiState.visibleActions) {
                        IconButton(onClick = { /* doSomething() */ }) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Favorite"
                            )
                        }
                    }
                }
            )
        }, content = { paddingValues ->
            var selectedUserForInfo by rememberSaveable { mutableStateOf<User?>(null) }

            ScaffoldContent(topInterval = paddingValues.calculateTopPadding()) {
                UserPhotosContent(
                    modifier = modifier,
                    paddingValues = paddingValues,
                    contentUiState = contentUiState,
                    photos = contentUiState.photos,
                    onShowUserInfo = { selectedUserForInfo = it },
                    onItemClicked = onItemClicked,
                    onLoadResult = { isSuccessful, message ->
                        contentUiState.setVisibleAction(isSuccessful)
                        if (!isSuccessful) {
                            contentUiState.baseUiState.scope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        }
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
                                    openCustomTab(contentUiState.baseUiState.context, portfolioUrl)
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
fun PhotosScreenPreview() {
    PhogalTheme {
        Scaffold(
            contentColor = Color.White,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(id = R.string.app_name),
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
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Profile"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* doSomething() */ }) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Localized description"
                            )
                        }
                    }
                )
            }
        ) {
        }
    }
}