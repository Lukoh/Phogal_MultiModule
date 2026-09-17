package com.goforer.phogal.presentation.stateholder.uistate.home.common.base

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.uistate.BaseUiState
import com.goforer.phogal.presentation.stateholder.uistate.ErrorEntity
import com.goforer.phogal.presentation.stateholder.uistate.home.common.BaseContentUiState

/**
 * Common state shared by photo-related ContentUiState implementations.
 *
 * BaseContentUiState
 *      └── BasePhotoContentUiState
 *              ├── SearchPhotosContentUiState
 *              ├── PopularPhotosContentUiState
 *              └── UserPhotosContentUiState
 */
@Stable
abstract class BasePhotoContentUiState(
    override val baseUiState: BaseUiState,
    initialVisible: Boolean,
    initialError: ErrorEntity?,
    initialSelectedUser: User?,
) : BaseContentUiState(
    baseUiState = baseUiState,
    initialError = initialError,
    initialSelectedUser = initialSelectedUser,
) {
    var visible: Boolean by mutableStateOf(initialVisible)
}