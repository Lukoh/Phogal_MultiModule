package com.goforer.phogal.presentation.ui.navigation.nav3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.goforer.phogal.presentation.ui.navigation.BottomNavRoute
import com.goforer.phogal.presentation.ui.navigation.Routes

@Stable
class NavigationState internal constructor(
    startRoute: BottomNavRoute,
    private val stacks: Map<BottomNavRoute, NavBackStack<NavKey>>,
) {
    /** Which tab is currently shown. */
    var currentRoute: BottomNavRoute by mutableStateOf(startRoute)
        private set

    /** Live back stack for whichever tab is selected. Feed this into `NavDisplay`. */
    val backStackForCurrentRoute: NavBackStack<NavKey>
        get() = stacks.getValue(currentRoute)

    /** True iff the current tab's back stack has more than just its root. */
    val canPopInCurrentRoute: Boolean
        get() = backStackForCurrentRoute.size > 1

    // ─────────────────────────── Tab switching ───────────────────────────

    /**
     * Switches to [route]. If the user taps the tab that is already active,
     * the tab's back stack is popped all the way back to its root — Material
     * bottom-nav convention ("second tap = home in this section").
     */
    fun selectRoute(route: BottomNavRoute) {
        if (currentRoute == route) popToTabRoot() else currentRoute = route
    }

    // ─────────────────────────── Navigation ───────────────────────────

    /** Pushes [route] onto the current tab's back stack. */
    fun push(route: NavKey) {
        backStackForCurrentRoute.add(route)
    }

    /**
     * Pops one entry off the current tab's back stack.
     * @return `true` if an entry was actually popped, `false` if the tab was
     *   already at its root (caller decides whether to exit the app).
     */
    fun pop(): Boolean {
        val stack = backStackForCurrentRoute
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    /** Pops to the tab's root regardless of current depth. */
    fun popToTabRoot() {
        val stack = backStackForCurrentRoute
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
    }

    companion object {
        /**
         * The default root screen route for each tab.
         *
         * Note the type split: the **map key** is a tab identity
         * ([BottomNavRoute]), while the **map value** is a screen route from
         * [Routes] — a different family of NavKeys. That separation keeps
         * "which tab am I in" cleanly distinct from "which screen inside
         * that tab is on top".
         */
        val DEFAULT_ROOTS: Map<BottomNavRoute, NavKey> = mapOf(
            BottomNavRoute.Gallery       to Routes.SearchPhotosRoute,
            BottomNavRoute.PopularPhotos to Routes.PopularPhotosRoute,
            BottomNavRoute.Notification  to Routes.NotificationsRoute,
            BottomNavRoute.Setting       to Routes.SettingRoute
        )

        /**
         * Stable short key for each tab. Used by the [Saver] to persist
         * [currentRoute] across config change / process death.
         *
         * Sealed interface objects don't have `.ordinal` like enums, so this
         * map is the explicit version of that concept. Using a fixed string
         * (not `toString()` or `qualifiedName`) keeps the wire format stable
         * if package names are ever refactored.
         */
        private val TAB_SAVE_KEYS: Map<BottomNavRoute, String> = mapOf(
            BottomNavRoute.Gallery       to "gallery",
            BottomNavRoute.PopularPhotos to "popular",
            BottomNavRoute.Notification  to "notification",
            BottomNavRoute.Setting       to "setting"
        )

        /** Saver for [currentRoute]. Stores a short key and looks it back up on restore. */
        internal fun tabSaver(): Saver<BottomNavRoute, String> = Saver(
            save = { TAB_SAVE_KEYS.getValue(it) },
            restore = { saved ->
                TAB_SAVE_KEYS.entries.first { it.value == saved }.key
            }
        )
    }
}

/**
 * Creates and remembers a [NavigationState] whose per-tab back stacks each
 * survive config changes and process death.
 */
@Composable
fun rememberNavigationState(
    initialRoute: BottomNavRoute = BottomNavRoute.Gallery
): NavigationState {
    // Per-tab stacks. rememberNavBackStack internally uses rememberSaveable
    // with a framework-provided Saver, so each stack is persisted for free.
    val galleryStack      = rememberNavBackStack(NavigationState.DEFAULT_ROOTS.getValue(BottomNavRoute.Gallery))
    val popularStack      = rememberNavBackStack(NavigationState.DEFAULT_ROOTS.getValue(BottomNavRoute.PopularPhotos))
    val notificationStack = rememberNavBackStack(NavigationState.DEFAULT_ROOTS.getValue(BottomNavRoute.Notification))
    val settingStack      = rememberNavBackStack(NavigationState.DEFAULT_ROOTS.getValue(BottomNavRoute.Setting))

    val stacks = remember(galleryStack, popularStack, notificationStack, settingStack) {
        mapOf(
            BottomNavRoute.Gallery       to galleryStack,
            BottomNavRoute.PopularPhotos to popularStack,
            BottomNavRoute.Notification  to notificationStack,
            BottomNavRoute.Setting       to settingStack
        )
    }

    val savedRoute = rememberSaveable(stateSaver = NavigationState.tabSaver()) {
        mutableStateOf(initialRoute)
    }

    val state = remember(stacks) {
        NavigationState(stacks = stacks, startRoute = savedRoute.value)
    }

    // Keep the saved tab in sync with the live currentTab so rotation / process
    // death restores whatever tab the user was on. Wrapped in SideEffect so the
    // write happens after composition commits (Compose rule: no state writes
    // during composition).
    SideEffect {
        savedRoute.value = state.currentRoute
    }

    return state
}
