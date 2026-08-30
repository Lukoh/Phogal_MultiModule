package com.goforer.phogal.presentation.stateholder.business.home.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goforer.base.utils.download.ImageDownloadManager
import com.goforer.phogal.data.model.remote.response.gallery.photo.download.TrackDownload
import com.goforer.phogal.data.repository.download.PhotoDownloadRepository
import com.goforer.phogal.presentation.stateholder.uistate.UiState
import com.goforer.phogal.presentation.stateholder.uistate.toUiStateStrict
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DownloadUiState {
    object Idle : DownloadUiState
    data class Loading(val progress: Float) : DownloadUiState // 진행률 포함
    object Success : DownloadUiState
    data class Error(val message: String) : DownloadUiState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class PhotoDownloadViewModel @Inject constructor(
    private val photoDownloadRepository: PhotoDownloadRepository,
    private val downloadManager: ImageDownloadManager
) : ViewModel() {
    private val _trackDownload = MutableStateFlow<UiState<TrackDownload>>(UiState.Idle)
    val trackDownload: StateFlow<UiState<TrackDownload>> = _trackDownload.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadUiState>(DownloadUiState.Idle)
    val downloadState: StateFlow<DownloadUiState> = _downloadState.asStateFlow()

    fun getDownloadPhotoUrl(id: String) {
        viewModelScope.launch {
            _trackDownload.value = UiState.Loading
            _trackDownload.value = photoDownloadRepository.getFinalDownloadUrl(id).toUiStateStrict()
        }
    }

    suspend fun downloadPhoto(url: String, fileName: String): Result<Unit> {
        _downloadState.value = DownloadUiState.Loading(0f)

        return downloadManager.downloadAndSavePhoto(
            photoUrl = url,
            fileName = "unsplash_${fileName}",
            onProgress = { currentProgress ->
                _downloadState.value = DownloadUiState.Loading(currentProgress)
            }
        )
    }
}