package com.goforer.phogal.presentation.stateholder.business.home.setting.follow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.repository.follow.FollowUserRepository
import com.goforer.phogal.di.dispatcher.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FollowViewModel @Inject constructor(
    private val followUserRepository: FollowUserRepository,
    @IoDispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val users: StateFlow<List<User>> = followUserRepository.getFollowingUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val followedUsers: StateFlow<PagingData<User>> = users
        .flatMapLatest { users ->
            followUserRepository.followedUsers(users, pageSize = PAGE_SIZE)
        }
        .cachedIn(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = PagingData.empty()
        )

    /**
     * Toggles follow status for [user].
     */
    fun setUserFollow(user: User) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                followUserRepository.toggleFollowingUser(user)
            }
        }
    }

    /**
     * Existence check for a followed user.
     * Note: View should ideally use [isUserFollowedFlow] to stay reactive.
     */
    fun isUserFollowed(user: User): Boolean {
        return users.value.any { it.id == user.id }
    }

    fun isUserFollowedFlow(user: User): Flow<Boolean> = followUserRepository.isUserFollowedFlow(user)

    private companion object {
        const val PAGE_SIZE = 20
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
