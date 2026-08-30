package com.goforer.phogal.presentation.stateholder.business.home.common.photo.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goforer.phogal.data.datasource.network.NetworkResult
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.data.repository.common.photo.info.PictureRepository
import com.goforer.phogal.data.repository.common.photo.like.PictureLikeRepository
import com.goforer.phogal.presentation.stateholder.uistate.UiState
import com.goforer.phogal.presentation.stateholder.uistate.toUiStateStrict
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Picture-detail ViewModel — MVVM + UDF on top of the Room SSOT stream.
 *
 * Upgrade note (Room + DAO migration):
 * [picture] is no longer filled by a one-shot REST call. It is derived from
 * [PictureRepository.getPictureStream], whose tail is a Room observable query.
 * Consequences:
 *  - Offline: a previously viewed photo renders instantly from the local DB.
 *  - Likes: [toggleLike] persists the change through the repository into Room;
 *    the DB emission updates [picture] automatically — the ViewModel no longer
 *    patches UI state by hand, keeping the data flow strictly unidirectional
 *    (View → intent → Repository → Room → Flow → StateFlow → View).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PictureViewModel @Inject constructor(
    private val pictureRepository: PictureRepository,
    private val pictureLikeRepository: PictureLikeRepository
) : ViewModel() {

    /**
     * The load request: id + a generation counter. Re-requesting the same id bumps
     * the generation, so `flatMapLatest` restarts the stream — this keeps the
     * original "loadPicture is idempotent but re-runs the request" semantics and
     * makes the UI's `onRetry = { loadPicture(id) }` work after an error.
     */
    private data class LoadRequest(val id: String, val generation: Int)

    private val loadRequest = MutableStateFlow<LoadRequest?>(null)

    /**
     * SSOT-backed picture state. Switching [loadRequest] switches the underlying
     * Room stream; every DB write for the current id re-emits here.
     */
    val picture: StateFlow<UiState<Picture>> = loadRequest
        .filterNotNull()
        .flatMapLatest { request ->
            pictureRepository.getPictureStream(request.id)
                .map<NetworkResult<Picture>, UiState<Picture>> { result -> result.toUiStateStrict() }
                .onStart { emit(UiState.Loading) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = UiState.Idle
        )

    private val _likeActionState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val likeActionState: StateFlow<UiState<Unit>> = _likeActionState.asStateFlow()

    private val _events = MutableSharedFlow<PictureUiEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<PictureUiEvent> = _events.asSharedFlow()

    /**
     * Selects the picture to observe (and refresh). Calling again with the same id
     * restarts the stream — the cached Room row still renders instantly while the
     * network refresh re-runs (used by the error screen's retry button).
     */
    fun loadPicture(id: String) {
        if (id.isBlank()) return
        val current = loadRequest.value
        loadRequest.value = if (current?.id == id) {
            current.copy(generation = current.generation + 1)
        } else {
            LoadRequest(id = id, generation = 0)
        }
    }

    /**
     * Toggles like on the currently loaded picture.
     *
     * Guards:
     *  - No-op unless a picture is successfully loaded.
     *  - No-op if a like action is already in flight (prevents rapid double-tap).
     *
     * On success the repository patches the Room row; the updated row flows back
     * into [picture] via the DAO's observable query.
     */
    fun toggleLike() {
        val current = (picture.value as? UiState.Success)?.data ?: return
        if (_likeActionState.value is UiState.Loading) return

        val wasLiked = current.likedByUser
        val id = current.id

        _likeActionState.value = UiState.Loading
        viewModelScope.launch {
            val result = if (wasLiked) {
                pictureLikeRepository.unlike(id)
            } else {
                pictureLikeRepository.like(id)
            }

            when (result) {
                is NetworkResult.Success, NetworkResult.Empty -> {
                    // Room row was patched by the repository — UI updates via the SSOT stream.
                    _likeActionState.value = UiState.Success(Unit)
                    _events.tryEmit(PictureUiEvent.LikeToggled(liked = !wasLiked))
                }
                is NetworkResult.Error -> {
                    _likeActionState.value = UiState.Error(code = result.code, message = result.message)
                    _events.tryEmit(PictureUiEvent.LikeFailed(result.message))
                }
                is NetworkResult.Exception -> {
                    val msg = result.throwable.message ?: "Network failure"
                    _likeActionState.value = UiState.Error(code = 0, message = msg)
                    _events.tryEmit(PictureUiEvent.LikeFailed(msg))
                }
            }
        }
    }

    /** Resets the transient like-action state back to Idle after the UI shows its feedback. */
    fun consumeLikeAction() {
        _likeActionState.value = UiState.Idle
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/** One-shot UI events from [PictureViewModel]. */
sealed interface PictureUiEvent {
    data class LikeToggled(val liked: Boolean) : PictureUiEvent
    data class LikeFailed(val message: String) : PictureUiEvent
}
