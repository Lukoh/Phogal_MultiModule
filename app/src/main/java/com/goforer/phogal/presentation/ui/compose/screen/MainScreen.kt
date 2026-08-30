package com.goforer.phogal.presentation.ui.compose.screen

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.goforer.designsystem.component.Background
import com.goforer.designsystem.component.GradientBackground
import com.goforer.designsystem.theme.GradientColors
import com.goforer.designsystem.theme.LocalGradientColors
import com.goforer.base.utils.connect.ConnectionUtils
import com.goforer.phogal.presentation.stateholder.uistate.rememberMainScreenUiState
import com.goforer.phogal.presentation.ui.navigation.BottomNavRoute
import com.goforer.phogal.presentation.ui.compose.screen.home.HomeScreen
import com.goforer.phogal.presentation.ui.compose.screen.home.OfflineScreen

@Composable
fun MainScreen(
    windowSizeClass: WindowSizeClass
) {
    val state = rememberMainScreenUiState(
        windowSizeClass = windowSizeClass
    )
    val shouldShowGradientBackground =
        state.currentTopLevelDestination == BottomNavRoute.Gallery

    Background {
        GradientBackground(
            gradientColors = if (shouldShowGradientBackground) {
                LocalGradientColors.current
            } else {
                GradientColors()
            }
        ) {
            val context = LocalContext.current

            if (!ConnectionUtils.isNetworkAvailable(context)) {
                OfflineScreen(modifier = Modifier)
            } else {
                HomeScreen(
                    modifier = Modifier,
                    shouldShowBottomBar = state.shouldShowBottomBar,
                    navigationState = state.navState
                )
            }
        }
    }
}
