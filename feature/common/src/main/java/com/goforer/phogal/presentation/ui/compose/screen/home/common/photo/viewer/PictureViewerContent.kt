package com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.size.Size
import com.goforer.designsystem.component.DownloadIndicator
import com.goforer.designsystem.component.dialog.AlertDialog
import com.goforer.designsystem.component.dialog.AutoDismissDialog
import com.goforer.designsystem.component.loadImagePainter
import com.goforer.base.utils.download.PhotoAlreadyExistsException
import com.goforer.designsystem.component.ErrorStatePlaceholder
import com.goforer.phogal.core.ui.R
import com.goforer.phogal.data.model.remote.response.gallery.photo.download.TrackDownload
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Exif
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.presentation.stateholder.uistate.UiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.PhotoContentUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.rememberUserContainerUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.UserContainer
import com.goforer.designsystem.theme.Blue75
import com.goforer.designsystem.theme.ColorSnowWhite
import com.goforer.designsystem.theme.ColorSystemGray1
import com.goforer.designsystem.theme.ColorSystemGray5
import com.goforer.designsystem.component.snsShimmer
import com.goforer.phogal.presentation.ui.compose.screen.home.common.photo.LoadingPicture
import com.goforer.designsystem.theme.DarkGreen60
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Immutable
sealed interface DownloadDialogState {
    object Idle : DownloadDialogState
    object Success : DownloadDialogState
    object Duplicate : DownloadDialogState
    data class Error(val message: String) : DownloadDialogState
}

@Composable
fun PictureViewerContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    contentUiState: PhotoContentUiState,
    onShowUserInfo: (User) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShownPhoto: (pictureUiState: Picture) -> Unit,
    onSuccess: (isSuccessful: Boolean) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        PictureBody(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            pictureState = contentUiState.pictureState,
            visibleViewButton = contentUiState.visibleViewButton,
            onShowUserInfo = onShowUserInfo,
            onViewPhotos = onViewPhotos,
            onShownPhoto = onShownPhoto,
            onSuccess = onSuccess,
            onClick = { url ->
                contentUiState.baseUiState.scope.launch {
                    contentUiState.photoDownloadViewModel.getDownloadPhotoUrl(url)
                    contentUiState.setShowPopup(true)
                }
            },
            onRetry = {
                contentUiState.pictureViewModel.loadPicture(contentUiState.id)
            }
        )

        if (contentUiState.showPopup) {
            DownloadIndicator(modifier, stringResource(R.string.picture_download_indicator))
        }

        DownloadPhoto(
            trackDownloadState = contentUiState.trackDownloadState,
            showPopup = contentUiState.showPopup,
            onRetry = { contentUiState.pictureViewModel.loadPicture(contentUiState.id) },
            onDismissPopup = { contentUiState.setShowPopup(false)},
            onDownload = { url ->
                contentUiState.baseUiState.scope.launch {
                    contentUiState.photoDownloadViewModel.downloadPhoto(url, contentUiState.id)
                        .onSuccess {
                            contentUiState.setDialogState(DownloadDialogState.Success)
                            contentUiState.setShowPopup(false)
                        }
                        .onFailure { error ->
                            contentUiState.setDialogState(
                                if (error is PhotoAlreadyExistsException) {
                                    DownloadDialogState.Duplicate
                                } else {
                                    DownloadDialogState.Error(
                                        error.message ?: contentUiState.baseUiState.context.getString(R.string.error_unknown)
                                    )
                                }
                            )
                            contentUiState.setShowPopup(false)
                        }
                }
            }
        )
    }

    ShowDialog(
        dialogState = contentUiState.dialogState,
        onDismiss = {
            contentUiState.setDialogState(DownloadDialogState.Idle)
            contentUiState.setShowPopup(false)
        },
        onDismissRequest = {
            contentUiState.setDialogState(DownloadDialogState.Idle)
            contentUiState.setShowPopup(false)
        },
    )
}

@Composable
fun PictureBody(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    pictureState: UiState<Picture>,
    visibleViewButton: Boolean,
    onShowUserInfo: (User) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShownPhoto: (pictureUiState: Picture) -> Unit,
    onSuccess: (isSuccessful: Boolean) -> Unit,
    onClick: (id: String) -> Unit,
    onRetry: () -> Unit
) {
    when (pictureState) {
        is UiState.Success -> {
            val picture = pictureState.data

            onSuccess(true)
            LaunchedEffect(picture.id) { onShownPhoto(picture) }
            PictureBodyContent(
                modifier = modifier,
                contentPadding = contentPadding,
                picture = picture,
                visibleViewButton = visibleViewButton,
                onShowUserInfo = onShowUserInfo,
                onViewPhotos = onViewPhotos,
                onShownPhoto = onShownPhoto,
                onClick = onClick,
            )
        }
        UiState.Loading, UiState.Idle -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) +
                            fadeIn() + expandIn(expandFrom = Alignment.TopStart),
                    exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) +
                            fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
                ) {
                    LoadingPicture(
                        modifier = Modifier.padding(4.dp, 4.dp),
                        enableLoadIndicator = true
                    )
                }
            }
        }
        is UiState.Error -> {
            onSuccess(false)
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) +
                            fadeIn() + expandIn(expandFrom = Alignment.TopStart),
                    exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) +
                            fadeOut() + shrinkOut(shrinkTowards = Alignment.TopStart)
                ) {
                    ErrorStatePlaceholder(
                        modifier = Modifier,
                        title = if (pictureState.code !in 200..299)
                            stringResource(id = R.string.error_dialog_network_title)
                        else
                            stringResource(id = R.string.error_dialog_title),
                        message = "${stringResource(id = R.string.error_get_picture)}${"\n\n"}${pictureState.message}",
                        onRetry = onRetry
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadPhoto(
    trackDownloadState: UiState<TrackDownload>,
    showPopup: Boolean,
    onRetry: () -> Unit,
    onDismissPopup: () -> Unit,
    onDownload: (url: String) -> Unit
) {
    LaunchedEffect(trackDownloadState) {
        if (trackDownloadState is UiState.Success) {
            onDownload(trackDownloadState.data.url)
        }
    }

    if (trackDownloadState is UiState.Error && showPopup) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeIn(),
                exit = scaleOut(transformOrigin = TransformOrigin(0.5f, 0.5f)) + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Card(
                    modifier = Modifier
                        .padding(48.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onDismissPopup()
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    ErrorStatePlaceholder(
                        modifier = Modifier.padding(16.dp),
                        isFullMaxSize = false,
                        title = if (trackDownloadState.code !in 200..299)
                            stringResource(id = R.string.error_dialog_network_title)
                        else
                            stringResource(id = R.string.error_dialog_title),
                        message = "${stringResource(id = R.string.error_get_picture)}\n\n${trackDownloadState.message}",
                        onRetry = onRetry
                    )
                }
            }
        }
    }
}

@Composable
fun PictureBodyContent(
    modifier: Modifier,
    contentPadding: PaddingValues,
    picture: Picture,
    visibleViewButton: Boolean,
    onShowUserInfo: (User) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShownPhoto: (picture: Picture) -> Unit,
    onClick: (id: String) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + 72.dp
                )
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BodyContent(
                modifier = Modifier,
                picture = picture,
                visibleViewPhotosButton = visibleViewButton,
                onShowUserInfo = onShowUserInfo,
                onViewPhotos = onViewPhotos,
                onShownPhoto = onShownPhoto,
                onClick = onClick
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ShowDialog(
    dialogState: DownloadDialogState,
    onDismiss: () -> Unit,
    onDismissRequest: () -> Unit
) {
    when (dialogState) {
        is DownloadDialogState.Success -> {
            AutoDismissDialog(
                message = stringResource(id = R.string.picture_download_complete),
                visible = true,
                onDismiss = onDismiss
            )
        }

        is DownloadDialogState.Duplicate -> {
            AlertDialog(
                title = stringResource(id = R.string.picture_download_notification),
                message = stringResource(id = R.string.picture_name_exist),
                onDismissRequest = onDismissRequest
            )
        }

        is DownloadDialogState.Error -> {
            AlertDialog(
                title = "Download Failed",
                message = dialogState.message,
                onDismissRequest = onDismissRequest
            )
        }

        DownloadDialogState.Idle -> { /* Noting */ }
    }
}

@Composable
fun BodyContent(
    modifier: Modifier = Modifier,
    picture: Picture,
    visibleViewPhotosButton: Boolean,
    onShowUserInfo: (User) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit,
    onShownPhoto: (picture: Picture) -> Unit,
    onClick: (id: String) -> Unit
) {
    var visibleCameraInfo by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .animateContentSize()
        ) {
            val imageUrl = picture.urls.regular
            val painter = loadImagePainter(
                data = imageUrl,
                size = Size.ORIGINAL
            )
            val bringIntoViewRequester = remember { BringIntoViewRequester() }

            if (painter.state is AsyncImagePainter.State.Loading) {
                val screenHeight = LocalWindowInfo.current.containerSize

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight.height.dp / 2)
                        .snsShimmer()
                )
            } else {
                UserContainer(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    state = rememberUserContainerUiState(
                        user = rememberSaveable { mutableStateOf(picture.user.toString()) },
                        profileSize = rememberSaveable { mutableDoubleStateOf(48.0) },
                        colors = remember { mutableStateOf(listOf(ColorSystemGray1, ColorSystemGray1, ColorSnowWhite, ColorSystemGray5, Blue75, DarkGreen60)) },
                        visibleViewButton = rememberSaveable { mutableStateOf(visibleViewPhotosButton) },
                        fromItem = rememberSaveable { mutableStateOf(false) }
                    ),
                    followViewModel = null,
                    onShowUserInfo = onShowUserInfo,
                    onViewPhotos = onViewPhotos
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(transformOrigin = TransformOrigin(0f, 0f)) + fadeIn(),
                        exit = scaleOut(transformOrigin = TransformOrigin(0f, 0f)) + fadeOut(),
                    ) {
                        ImageContent(
                            painter = painter,
                            onClick = { onClick(picture.id) }
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BehaviorBar(likes = 700, downloads = 700, views = 700)
                    val description = picture.description
                    if (!description.isNullOrEmpty()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Normal,
                                lineHeight = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        picture.location?.let {
                            MetadataItem(icon = Icons.Outlined.Place, text = it.name)
                        }
                        MetadataItem(
                            icon = Icons.Outlined.DateRange,
                            text = "${picture.createdAt} ${stringResource(id = R.string.picture_posted)}"
                        )
                    }

                    picture.exif?.let { exif ->
                        LaunchedEffect(visibleCameraInfo) {
                            if (visibleCameraInfo) {
                                delay(1500.milliseconds)
                                bringIntoViewRequester.bringIntoView()
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                visibleCameraInfo = !visibleCameraInfo
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (visibleCameraInfo)
                                    stringResource(id = R.string.picture_close_camera_info)
                                else
                                    stringResource(id = R.string.picture_view_camera_info),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        AnimatedVisibility(
                            visible = visibleCameraInfo,
                            enter = fadeIn() + expandVertically(animationSpec = tween(300)),
                            exit = fadeOut() + shrinkVertically(animationSpec = tween(300))
                        ) {
                            Box(modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester)) {
                                ExifCard(exif = exif)
                            }
                        }
                    }
                }

                LaunchedEffect(picture.id) {
                    onShownPhoto(picture)
                }
            }
        }
    }
}

@Composable
fun ImageContent(
    modifier: Modifier = Modifier,
    painter: AsyncImagePainter,
    onClick: () -> Unit
) {
    val transition by animateFloatAsState(
        targetValue = if (painter.state is AsyncImagePainter.State.Success) 1f else 0f,
        animationSpec = tween(durationMillis = 400)
    )

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .then(
                ((painter.state as? AsyncImagePainter.State.Success)
                    ?.painter
                    ?.intrinsicSize
                    ?.let { intrinsicSize ->
                        Modifier.aspectRatio(intrinsicSize.width / intrinsicSize.height)
                    } ?: Modifier)
            )
            .clip(RectangleShape)
            .clickable { onClick() }
            .scale(.95f + (.05f * transition))
            .graphicsLayer { rotationX = (1f - transition) * 3f }
            .alpha(transition),
        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
            setToSaturation(transition)
        })
    )
}

@Composable
fun BehaviorBar(likes: Long, downloads: Long, views: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(icon = Icons.Rounded.FavoriteBorder, value = likes.toString(), label = stringResource(id = R.string.picture_likes))
        StatItem(icon = Icons.Rounded.Download, value = downloads.toString(), label = stringResource(id = R.string.picture_downloads))
        StatItem(icon = Icons.Rounded.Visibility, value = views.toString(), label = stringResource(id = R.string.picture_views))
    }
}

@Composable
fun StatItem(icon: ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$value $label",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MetadataItem(icon: ImageVector, text: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ExifCard(exif: Exif) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = exif.name ?: stringResource(id = R.string.picture_no_camera_name),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            exif.name?.let {
                Text(
                    text = "Lens • ƒ/${exif.aperture}  ${exif.focalLength}mm  ${exif.exposureTime}s  ISO ${exif.iso}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 20.sp
                )
            }
        }
    }
}