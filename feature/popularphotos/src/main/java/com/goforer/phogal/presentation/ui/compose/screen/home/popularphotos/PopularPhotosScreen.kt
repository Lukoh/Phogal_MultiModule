package com.goforer.phogal.presentation.ui.compose.screen.home.popularphotos

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.goforer.base.utils.connect.ConnectionUtils
import com.goforer.designsystem.component.CardSnackBar
import com.goforer.designsystem.component.CustomCenterAlignedTopAppBar
import com.goforer.designsystem.component.ScaffoldContent
import com.goforer.designsystem.theme.ColorBgSecondary
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.presentation.stateholder.uistate.PagingResult
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.uistate.home.popularphotos.PopularPhotosContentUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.OfflineScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.UserInfoBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularPhotosScreen(
    modifier: Modifier = Modifier,
    contentUiState: PopularPhotosContentUiState,
    isUserFollowed: (User) -> Boolean,
    onToggleFollow: (User) -> Unit,
    onItemClicked: (id: String, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onOpenWebView: (firstName: String, url: String) -> Unit,
    onStart: () -> Unit = {
        //To Do:: Implement the code what you want to do....
    },
    onStop: () -> Unit = {
        //To Do:: Implement the code what you want to do....
    },
) {
    if (!ConnectionUtils.isNetworkAvailable(contentUiState.baseUiState.context)) {
        OfflineScreen(modifier = Modifier)
    } else {
        val currentOnStart by rememberUpdatedState(onStart)
        val currentOnStop by rememberUpdatedState(onStop)
        val snackbarHostState = remember { SnackbarHostState() }
        val backHandlingEnabled by remember { mutableStateOf(value = true) }
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

        BackHandler(backHandlingEnabled) {
            (contentUiState.baseUiState.context as Activity).finish()
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
                            stringResource(id = R.string.bottom_navigation_popular_photos),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 20.sp,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {},
                    actions = {}
                )
            }, content = { paddingValues ->
                var selectedUserForInfo by rememberSaveable { mutableStateOf<User?>(null) }
                val layoutDirection = LocalLayoutDirection.current

                ScaffoldContent(topInterval = paddingValues.calculateTopPadding()) {
                    PopularPhotosContent(
                        modifier = modifier,
                        paddingValues= PaddingValues(
                            start = paddingValues.calculateStartPadding(layoutDirection),
                            top = 0.dp,
                            end = paddingValues.calculateEndPadding(layoutDirection),
                            bottom = paddingValues.calculateBottomPadding()
                        ),
                        photos = contentUiState.photos,
                        isUserFollowed = isUserFollowed,
                        onToggleFollow = onToggleFollow,
                        onShowUserInfo = { selectedUserForInfo = it },
                        onItemClicked = onItemClicked,
                        onViewPhotos = onViewPhotos,
                        onLoadResult = { result ->
                            val isSuccessful = result is PagingResult.Success
                            val message = when (result) {
                                is PagingResult.Success -> result.message
                                is PagingResult.Error -> result.error.message ?: ""
                                else -> ""
                            }

                            contentUiState.setVisibleActions(isSuccessful)
                            if (result is PagingResult.Error) {
                                contentUiState.baseUiState.scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        },
                        onLoadedPhotos = {
                            contentUiState.setLoadedPhotos(it)
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