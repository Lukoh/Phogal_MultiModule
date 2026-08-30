package com.goforer.phogal.presentation.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.goforer.phogal.core.ui.R
import kotlinx.serialization.Serializable

/**
 * Top-level bottom-navigation destinations for Phogal — **Navigation 3 1.1.0**.
 *
 * Previously this was a `BottomNavDestination` enum that paired a tab label
 * and icon but had no relation to the navigation system. Under Nav3 it is
 * now a **sealed interface that extends [androidx.navigation3.runtime.NavKey]**, which means each tab
 * identity *is itself* a valid back stack entry and a type-safe navigation key.
 *
 * ## Why sealed interface + NavKey (not enum)?
 *
 *  1. **Type safety as nav keys** — Any `BottomNavRoute` can be pushed onto
 *     a `NavBackStack` directly. No enum→route conversion layer needed.
 *
 *  2. **Per-tab parameters later** — If one tab ever needs arguments
 *     (e.g. `Settings(userId: String)`), you can change that single `object`
 *     to a `data class` without touching the others. Enums can't carry
 *     heterogeneous payloads cleanly.
 *
 *  3. **Exhaustive `when` still works** — `sealed interface` gives the same
 *     exhaustiveness guarantees as `enum` in `when (tab)` expressions.
 *
 *  4. **Kotlin Multiplatform friendly** — Nav3 is KMP-capable, and
 *     `@Serializable` sealed hierarchies travel to other platforms unchanged.
 *
 * ## Tab identity vs tab content
 *
 * Be careful not to confuse these two concepts:
 *
 *  - [BottomNavRoute] = **tab identity** (which bottom-nav pill is selected).
 *    Used by `PhogalNavState.currentTab` and stored as a map key for per-tab
 *    back stacks.
 *
 *  - `Routes.SearchPhotosRoute`, `Routes.PopularPhotosRoute`, etc. =
 *    **screen routes inside each tab**. These are the NavKeys actually pushed
 *    onto the NavBackStack for rendering via `entry<T>`.
 *
 * The two are distinct on purpose: a tab can contain many screens (think
 * Gallery → Search → Picture detail → User photos), but it has exactly one
 * bottom-nav identity.
 */
@Serializable
sealed interface BottomNavRoute : NavKey {

    /** Resource id for the tab icon shown in the Material 3 NavigationBar. */
    @get:DrawableRes
    val icon: Int

    /** Resource id for the tab label / content description. */
    @get:StringRes
    val title: Int

    @Serializable
    data object Gallery : BottomNavRoute {
        override val icon: Int get() = R.drawable.ic_photo
        override val title: Int get() = R.string.bottom_navigation_gallery
    }

    @Serializable
    data object PopularPhotos : BottomNavRoute {
        override val icon: Int get() = R.drawable.ic_popphotos
        override val title: Int get() = R.string.bottom_navigation_popular_photos
    }

    @Serializable
    data object Notification : BottomNavRoute {
        override val icon: Int get() = R.drawable.ic_notification
        override val title: Int get() = R.string.bottom_navigation_notification
    }

    @Serializable
    data object Setting : BottomNavRoute {
        override val icon: Int get() = R.drawable.ic_setting
        override val title: Int get() = R.string.bottom_navigation_setting
    }

    companion object {
        /**
         * All top-level tabs in bottom-nav display order.
         *
         * Kept as a stable immutable list (cheap to remember) so the Compose
         * `BottomNavBar` composable doesn't re-allocate on every recomposition.
         * This is the sealed-interface equivalent of Kotlin 1.9+ `enum entries`.
         */
        val entries: List<BottomNavRoute> = listOf(
            Gallery,
            PopularPhotos,
            Notification,
            Setting
        )
    }
}