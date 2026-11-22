package com.goforer.phogal.presentation.stateholder.business.home.common.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.repository.common.user.photos.UserPhotosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserPhotosViewModel @Inject constructor(
    private val userPhotosRepository: UserPhotosRepository
) : ViewModel() {
    private data class UserRequest(val username: String, val sessionId: Int)

    private val _userRequest = MutableStateFlow(UserRequest("", 0))

    /**
     * The Unsplash username whose photos are currently being viewed.
     */
    val username: StateFlow<String> = _userRequest
        .map { it.username }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    /**
     * Stream of paged photos for the current [username].
     * Blank usernames result in an empty paging stream.
     */
    val photos: StateFlow<PagingData<Photo>> = _userRequest
        .transformLatest { (user, _) ->
            if (user.isBlank()) {
                emit(PagingData.empty())
            } else {
                userPhotosRepository.userPhotos(user, PAGE_SIZE).collect { pagingData ->
                    emit(pagingData)
                }
            }
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = PagingData.empty()
        )

    /** 
     * Sets which user we're viewing. 
     * 
     * Safe to call multiple times; redundant updates with the same [name] 
     * will not trigger a new network request.
     */
    fun loadFor(name: String) {
        val current = _userRequest.value
        if (current.username != name) {
            _userRequest.value = UserRequest(username = name, sessionId = current.sessionId + 1)
        }
    }

    /**
     * Forces a refresh of the photo stream for the current user.
     */
    fun refresh() {
        val current = _userRequest.value
        if (current.username.isNotBlank()) {
            _userRequest.value = current.copy(sessionId = current.sessionId + 1)
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
