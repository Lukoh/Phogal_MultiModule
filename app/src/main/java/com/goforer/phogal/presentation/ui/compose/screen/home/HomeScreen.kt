package com.goforer.phogal.presentation.ui.compose.screen.home

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.goforer.designsystem.theme.ColorBgSecondary
import com.goforer.phogal.presentation.ui.navigation.BottomNavRoute
import com.goforer.phogal.presentation.ui.navigation.nav3.LocalSharedTransitionScope
import com.goforer.phogal.presentation.ui.navigation.nav3.NavigationState
import com.goforer.phogal.presentation.ui.navigation.nav3.phogalEntries
import com.goforer.phogal.presentation.ui.navigation.nav3.rememberNavigationState

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    shouldShowBottomBar: Boolean,
    navigationState: NavigationState = rememberNavigationState()
) {
    val bottomBarVisible = !navigationState.canPopInCurrentRoute
    val bottomBarOffset: Dp = if (bottomBarVisible) 0.dp else 80.dp

    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    val sceneStrategies = remember(listDetailStrategy) {
        listOf(
            DialogSceneStrategy(),
            listDetailStrategy,
            SinglePaneSceneStrategy()
        )
    }

    val stateHolderDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
    val viewModelStoreDecorator = rememberViewModelStoreNavEntryDecorator<NavKey>()
    val entryDecorators = remember(stateHolderDecorator, viewModelStoreDecorator) {
        listOf(stateHolderDecorator, viewModelStoreDecorator)
    }

    Scaffold(
        modifier = modifier,
        containerColor = ColorBgSecondary,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (shouldShowBottomBar) {
                BottomNavBar(
                    currentRoute = navigationState.currentRoute,
                    visible = bottomBarVisible,
                    offset = bottomBarOffset,
                    onTabSelected = navigationState::selectRoute
                )
            }
        },
        content = { innerPadding ->
            Box(
                Modifier.padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    top = 0.dp,
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = if (bottomBarVisible) innerPadding.calculateBottomPadding() else 0.dp
                )
            ) {
                SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        val currentTab = navigationState.currentRoute
                        val currentBackStack = navigationState.backStackForCurrentRoute

                        Box(modifier = Modifier.fillMaxSize()) {
                            key(currentTab) {
                                NavDisplay(
                                    backStack = currentBackStack,
                                    onBack = { navigationState.pop() },
                                    sceneStrategies = sceneStrategies,
                                    entryDecorators = entryDecorators,
                                    transitionSpec = DefaultTransitions.push,
                                    popTransitionSpec = DefaultTransitions.pop,
                                    predictivePopTransitionSpec = DefaultTransitions.predictivePop,
                                    entryProvider = entryProvider { phogalEntries(navigationState) }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

// ─────────────────────────── Transition specs (extracted) ───────────────────────────

@Stable
private object DefaultTransitions {
    private const val DURATION_MS = 300
    private const val PREDICTIVE_DURATION_MS = 250

    val push: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        val enter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(DURATION_MS)
        ) + fadeIn(tween(DURATION_MS))
        val exit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(DURATION_MS)
        ) + fadeOut(tween(DURATION_MS))
        enter togetherWith exit
    }

    val pop: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        val enter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(DURATION_MS)
        ) + fadeIn(tween(DURATION_MS))
        val exit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(DURATION_MS)
        ) + fadeOut(tween(DURATION_MS))
        enter togetherWith exit
    }

    val predictivePop: AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform = { _ ->
        val enter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(PREDICTIVE_DURATION_MS)
        ) + fadeIn(tween(PREDICTIVE_DURATION_MS))
        val exit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = tween(PREDICTIVE_DURATION_MS)
        ) + fadeOut(tween(PREDICTIVE_DURATION_MS))
        enter togetherWith exit
    }
}

// ─────────────────────────── Bottom bar (extracted) ───────────────────────────

@Composable
private fun BottomNavBar(
    currentRoute: BottomNavRoute,
    visible: Boolean,
    offset: Dp,
    onTabSelected: (BottomNavRoute) -> Unit
) {
    val items = remember { BottomNavRoute.entries }

    Column(
        modifier = if (visible) {
            Modifier.navigationBarsPadding()
        } else {
            Modifier.offset { IntOffset(x = 0, y = offset.value.toInt()) }
        }
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )

        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            modifier = Modifier.height(64.dp)
        ) {
            items.forEach { item ->
                val selected = currentRoute == item
                val animatedScale by animateFloatAsState(
                    targetValue = if (selected) 1.15f else 1.0f,
                    animationSpec = tween(durationMillis = 200),
                    label = "IconScale"
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = stringResource(id = item.title),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .scale(animatedScale)
                        )
                    },
                    selected = selected,
                    alwaysShowLabel = false,
                    onClick = { onTabSelected(item) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSurface,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
