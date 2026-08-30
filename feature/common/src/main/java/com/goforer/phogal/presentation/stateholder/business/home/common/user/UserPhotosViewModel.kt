package com.goforer.phogal.presentation.stateholder.business.home.common.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import com.goforer.phogal.data.repository.common.user.photos.UserPhotosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserPhotosViewModel @Inject constructor(
    private val userPhotosRepository: UserPhotosRepository
) : ViewModel() {
    private val _username = MutableStateFlow("")

    /**
     * The Unsplash username whose photos are currently being viewed.
     */
    val username: StateFlow<String> = _username.asStateFlow()

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 0)

    /**
     * Stream of paged photos for the current [username].
     * Blank usernames result in an empty paging stream.
     */
    val photos: StateFlow<PagingData<Photo>> = combine(
        _username,
        _refreshTrigger.onStart { emit(Unit) }
    ) { user, _ -> user }
        .flatMapLatest { user ->
            if (user.isBlank()) {
                flowOf(PagingData.empty())
            } else {
                userPhotosRepository.userPhotos(user, PAGE_SIZE)
            }
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PagingData.empty()
        )

    /** 
     * Sets which user we're viewing. 
     * 
     * Safe to call multiple times; redundant updates with the same [name] 
     * will not trigger a new network request.
     */
    fun loadFor(name: String) {
        if (_username.value != name) {
            _username.value = name
        }
    }

    /**
     * Forces a refresh of the photo stream for the current user.
     */
    fun refresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
