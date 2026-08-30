package com.goforer.phogal.presentation.stateholder.business.home.setting.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goforer.phogal.data.datasource.local.LocalDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingViewModel @Inject constructor(
    private val localDataSource: LocalDataSource
) : ViewModel() {

    enum class NotificationChannel {
        Following, Latest, Community
    }

    val followingEnabled: StateFlow<Boolean> = localDataSource.enabledFollowingNotificationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), true)

    val latestEnabled: StateFlow<Boolean> = localDataSource.enabledLatestNotificationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), true)

    val communityEnabled: StateFlow<Boolean> = localDataSource.enabledCommunityNotificationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), true)

    fun setEnabled(channel: NotificationChannel, enabled: Boolean) {
        viewModelScope.launch {
            when (channel) {
                NotificationChannel.Following -> localDataSource.setFollowingNotificationEnabled(enabled)
                NotificationChannel.Latest    -> localDataSource.setLatestNotificationEnabled(enabled)
                NotificationChannel.Community -> localDataSource.setCommunityNotificationEnabled(enabled)
            }
        }
    }

    fun isEnabled(channel: NotificationChannel): Boolean = when (channel) {
        NotificationChannel.Following -> followingEnabled.value
        NotificationChannel.Latest    -> latestEnabled.value
        NotificationChannel.Community -> communityEnabled.value
    }

    // TODO: Connect these to actual DataStore preferences if needed for a global toggle.
    // Currently these are stubs to maintain compatibility with NotificationSettingContent.
    fun setNotificationEnabled(toggled: Boolean) {
        setEnabled(NotificationChannel.Following, toggled)
        setEnabled(NotificationChannel.Latest, toggled)
        setEnabled(NotificationChannel.Community, toggled)
    }

    fun getNotificationSetting(): Boolean {
        return followingEnabled.value || latestEnabled.value || communityEnabled.value
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
