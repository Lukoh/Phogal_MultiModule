package com.goforer.phogal.presentation.stateholder.uistate.home.common.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.rememberBaseUiState

@Stable
class UserContainerUiState internal constructor(
    val baseUiState: BaseUiState,
    val user: String,
    initialProfileSize: Double,
    initialColors: List<Color>,
    initialVisibleViewButton: Boolean,
    initialFromItem: Boolean,
) {
    var profileSize: Double by mutableDoubleStateOf(initialProfileSize)
    var colors: List<Color> by mutableStateOf(initialColors)
    var visibleViewButton: Boolean by mutableStateOf(initialVisibleViewButton)
    var fromItem: Boolean by mutableStateOf(initialFromItem)

    companion object {
        fun Saver(baseUiState: BaseUiState): Saver<UserContainerUiState, *> = listSaver(
            save = {
                listOf(it.user, it.profileSize, it.colors.map { color -> color.value.toLong() }, it.visibleViewButton, it.fromItem)
            },
            restore = {
                UserContainerUiState(
                    baseUiState = baseUiState,
                    user = it[0] as String,
                    initialProfileSize = it[1] as Double,
                    initialColors = (it[2] as List<*>).map { value -> Color((value as Long).toULong()) },
                    initialVisibleViewButton = it[3] as Boolean,
                    initialFromItem = it[4] as Boolean
                )
            }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun rememberUserContainerUiState(
    baseUiState: BaseUiState = rememberBaseUiState(),
    user: String = "",
    initialProfileSize: Double = 0.0,
    initialColors: List<Color> = emptyList(),
    initialVisibleViewButton: Boolean = false,
    initialFromItem: Boolean = false
): UserContainerUiState = rememberSaveable(baseUiState, saver = UserContainerUiState.Saver(baseUiState)) {
    UserContainerUiState(
        baseUiState = baseUiState,
        user = user,
        initialProfileSize = initialProfileSize,
        initialColors = initialColors,
        initialVisibleViewButton = initialVisibleViewButton,
        initialFromItem = initialFromItem
    )
}
