package com.goforer.phogal.presentation.ui.compose.screen.home.setting.bookmark

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.size.Size
import com.goforer.designsystem.component.loadImagePainter
import com.goforer.designsystem.component.snsShimmer
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture
import com.goforer.phogal.presentation.stateholder.business.home.setting.follow.FollowViewModel
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.PictureItemUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.photo.rememberPictureItemUiState
import com.goforer.phogal.presentation.stateholder.uistate.home.common.user.rememberUserContainerUiState
import com.goforer.phogal.presentation.ui.compose.screen.home.common.user.UserContainer
import com.goforer.designsystem.theme.Blue75
import com.goforer.designsystem.theme.ColorSnowWhite
import com.goforer.phogal.data.model.remote.response.gallery.common.Links
import com.goforer.phogal.data.model.remote.response.gallery.common.ProfileImage
import com.goforer.phogal.data.model.remote.response.gallery.common.Social
import com.goforer.phogal.data.model.remote.response.gallery.common.Urls
import com.goforer.phogal.data.model.remote.response.gallery.common.user.User
import com.goforer.phogal.data.model.remote.response.gallery.common.user.UserLinks
import com.goforer.designsystem.theme.ColorSystemGray1
import com.goforer.designsystem.theme.ColorSystemGray5
import com.goforer.designsystem.theme.DarkGreen60
import com.goforer.designsystem.theme.PhogalTheme

@Composable
fun PictureItem(
    modifier: Modifier = Modifier,
    pictureItemUiState: PictureItemUiState = rememberPictureItemUiState(),
    followViewModel: FollowViewModel?,
    onShowUserInfo: (User) -> Unit,
    onItemClicked: (item: Picture, index: Int) -> Unit,
    onViewPhotos: (name: String, firstName: String, lastName: String, username: String) -> Unit
) {
    val picture = pictureItemUiState.picture
    val topPadding = if (pictureItemUiState.index == 0) 16.dp else 8.dp
    val bottomPadding = 8.dp

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = bottomPadding)
            .clickable {
                pictureItemUiState.setClicked(true)
                onItemClicked(picture, pictureItemUiState.index)
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (pictureItemUiState.clicked)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            val imageUrl = picture.urls.raw
            val painter = loadImagePainter(
                data = imageUrl,
                size = Size(picture.width / 8,picture.height / 8)
            )

            val transition by animateFloatAsState(
                targetValue = if (painter.state is AsyncImagePainter.State.Success) 1f else 0f,
                animationSpec = tween(durationMillis = 400)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        ratio = if (picture.width > 0 && picture.height > 0)
                            picture.width.toFloat() / picture.height.toFloat()
                        else 4f / 3f
                    )
            ) {
                if (painter.state is AsyncImagePainter.State.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .snsShimmer()
                    )
                } else {
                    val imageModifier = Modifier
                        .fillMaxSize()
                        .scale(0.96f + (0.04f * transition))
                        .graphicsLayer { rotationX = (1f - transition) * 3f }
                        .alpha(transition)

                    Image(
                        painter = painter,
                        contentDescription = "Picture by ${picture.user.name}",
                        contentScale = ContentScale.Crop,
                        modifier = imageModifier,
                        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                            setToSaturation(transition)
                        })
                    )
                }
            }

            UserContainer(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                state = rememberUserContainerUiState(
                    user = rememberSaveable { mutableStateOf(picture.user.toString()) },
                    profileSize = rememberSaveable { mutableDoubleStateOf(36.0) },
                    colors = remember { mutableStateOf(listOf(ColorSystemGray1, ColorSystemGray1, ColorSnowWhite, ColorSystemGray5, Blue75, DarkGreen60)) },
                    visibleViewButton = rememberSaveable { mutableStateOf(pictureItemUiState.visibleViewButton) },
                    fromItem = rememberSaveable { mutableStateOf(true) }
                ),
                followViewModel = followViewModel,
                onShowUserInfo = onShowUserInfo,
                onViewPhotos = onViewPhotos
            )
        }
    }
}

@Preview(name = "Light Mode - Card", showBackground = true)
@Preview(
    name = "Dark Mode - Card",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PictureItemPreview() {
    val mockPicture = PreviewMockData.createMockPicture()
    val mockUiState = rememberPictureItemUiState(
        picture = remember { mutableStateOf(mockPicture) },
        index = remember { mutableIntStateOf(0) },
        visibleViewButton = remember { mutableStateOf(true) }
    )

    PhogalTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                PictureItem(
                    pictureItemUiState = mockUiState,
                    followViewModel = null,
                    onShowUserInfo = {},
                    onItemClicked = { _, _ ->  },
                    onViewPhotos = { _, _, _, _ -> }
                )
            }
        }
    }
}

object PreviewMockData {
    fun createMockPicture(): Picture {
        return Picture(
            id = "mock_picture_id_01",
            createdAt = "2026-05-21T00:00:00Z",
            updatedAt = "2026-05-21T12:00:00Z",
            width = 1920,
            height = 1080,
            color = "#E0E8F5",
            blurHash = "LKO2?4%gWZ_4_w_4ofxtTg_3RjM{",
            downloads = 1250,
            publicDomain = true,
            description = "Beautiful landscape photo for preview presentation",
            exif = null,
            location = null,
            tags = emptyList(),
            currentUserCollections = emptyList(),
            urls = Urls(
                raw = "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
                full = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80",
                regular = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=1080",
                small = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=400",
                thumb = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=200"
            ),
            links = Links(
                self = "https://api.unsplash.com/photos/mock",
                html = "https://unsplash.com/photos/mock",
                download = "https://unsplash.com/photos/mock/download",
                downloadLocation = "https://api.unsplash.com/photos/mock/download"
            ),
            user = User(
                acceptedTos = true,
                bio = "Technical Project Manager & Automotive Android Engineer",
                firstName = "Lukoh",
                forHire = false,
                id = "user_id_lukoh",
                instagramUsername = "namlukoh_insta",
                lastName = "Nam",
                location = "Seoul, South Korea",
                name = "Lukoh Nam",
                portfolioUrl = "https://medium.com/@namlukoh",
                totalCollections = 14,
                totalLikes = 245,
                totalPhotos = 82,
                twitterUsername = null,
                updatedAt = "2026-05-21T18:00:00Z",
                username = "namlukoh",
                profileImage = ProfileImage(
                    small = "https://images.unsplash.com/profile-mock-s",
                    medium = "https://images.unsplash.com/profile-mock-m",
                    large = "https://images.unsplash.com/profile-mock-l"
                ),
                links = UserLinks(
                    self = "https://api.unsplash.com/users/namlukoh",
                    html = "https://unsplash.com/@namlukoh",
                    photos = "https://api.unsplash.com/users/namlukoh/photos"
                ),
                social = Social(
                    instagramUsername = "namlukoh_insta",
                    portfolioUrl = "https://medium.com/@namlukoh",
                    twitterUsername = null,
                    paypalEmail = null
                )
            ),
            likedByUser = false,
            bookmarked = false
        )
    }
}
