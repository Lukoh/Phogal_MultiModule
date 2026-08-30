package com.goforer.phogal.presentation.stateholder.uistate

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.goforer.phogal.presentation.ui.navigation.BottomNavRoute
import com.goforer.phogal.presentation.ui.navigation.nav3.NavigationState
import com.goforer.phogal.presentation.ui.navigation.nav3.rememberNavigationState
import kotlinx.coroutines.CoroutineScope
/**
 * Top-level screen state for Phogal.
 *
 * Nav3 1.1.0 edition — there is no `NavHostController`. Navigation state lives
 * entirely inside [navState] ([NavigationState]). `currentTopLevelDestination`
 * is read synchronously from that state — no `@Composable get()` required
 * because the underlying value is plain Compose state, not a suspend/observable.
 */
@Stable
class MainScreenUiState(
    val navState: NavigationState,
    val coroutineScope: CoroutineScope,
    val windowSizeClass: WindowSizeClass,
) {
    val currentTopLevelDestination: BottomNavRoute
        get() = navState.currentRoute

    val shouldShowBottomBar: Boolean
        get() = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
}

@Composable
fun rememberMainScreenUiState(
    windowSizeClass: WindowSizeClass,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navState: NavigationState = rememberNavigationState(initialRoute = BottomNavRoute.Gallery)
): MainScreenUiState = remember(navState, coroutineScope, windowSizeClass) {
    MainScreenUiState(
        navState = navState,
        coroutineScope = coroutineScope,
        windowSizeClass = windowSizeClass
    )
}
