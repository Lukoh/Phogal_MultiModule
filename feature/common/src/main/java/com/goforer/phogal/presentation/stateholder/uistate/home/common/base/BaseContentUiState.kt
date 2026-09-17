package com.goforer.phogal.presentation.stateholder.uistate.home.common

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.ErrorEntity

/**
 * Common state shared by Home ContentUiState implementations.
 *
 * Only semantically common state is centralized here.
 * Feature-specific state remains in each concrete ContentUiState.
 */
@Stable
abstract class BaseContentUiState(
    open val baseUiState: BaseUiState,
    initialError: ErrorEntity?,
    initialSelectedUser: User?,
) {
    var error: ErrorEntity? by mutableStateOf(initialError)
    var selectedUser: User? by mutableStateOf(initialSelectedUser)
}
